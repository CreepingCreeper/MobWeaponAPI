package dev.xkmc.mob_weapon_api.integration.tinker;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.ItemHandlerHelper;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.ranged.BowAmmoModifierHook;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.function.Predicate;

import static slimeknights.tconstruct.library.modifiers.hook.ranged.BowAmmoModifierHook.SKIP_INVENTORY_AMMO;

public class GolemTinkerAmmoHook {

    private static ItemStack findMatchingAmmo(ItemStack bow, LivingEntity living, Predicate<ItemStack> predicate) {
        for(InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = living.getItemInHand(hand);
            if (stack != bow && predicate.test(stack)) {
                return ForgeHooks.getProjectile(living, bow, stack);
            }
        }

        return ItemStack.EMPTY;
    }

    static ItemStack consumeAmmo(IToolStackView tool, ItemStack bow, LivingEntity living, boolean noConfuse, @Nullable Predicate<ItemStack> predicate) {
        Level level = living.level();
        boolean skipInventoryAmmo = tool.getVolatileData().getBoolean(SKIP_INVENTORY_AMMO);
        ItemStack standardAmmo;
        if (skipInventoryAmmo) {
            standardAmmo = ItemStack.EMPTY;
        } else if (predicate == null) {
            standardAmmo = ForgeHooks.getProjectile(living, bow, ItemStack.EMPTY);
        } else {
            standardAmmo = living.getProjectile(bow);
        }

        ItemStack resultStack = ItemStack.EMPTY;
        if (predicate != null) {
            for(ModifierEntry entry : tool.getModifierList()) {
                BowAmmoModifierHook hook = entry.getHook(ModifierHooks.BOW_AMMO);
                ItemStack ammo = hook.findAmmo(tool, entry, living, standardAmmo, predicate);
                if (!ammo.isEmpty()) {
                    if (noConfuse) {
                        ToolDamageUtil.damageAnimated(ToolStack.from(ammo), 1, living);
                        return ItemHandlerHelper.copyStackWithSize(ammo, 1);
                    }

                    resultStack = ItemHandlerHelper.copyStackWithSize(ammo, Math.min(1, ammo.getCount()));
                    hook.shrinkAmmo(tool, entry, living, ammo, resultStack.getCount());
                    break;
                }
            }
        }

        if (resultStack.isEmpty()) {
            if (standardAmmo.isEmpty()) {
                return ItemStack.EMPTY;
            }

            if (noConfuse) {
                ToolDamageUtil.damageAnimated(ToolStack.from(standardAmmo), 1, living);
                return ItemHandlerHelper.copyStackWithSize(standardAmmo, 1);
            }

            resultStack = standardAmmo.split(1);

        }

        if (resultStack.getCount() < 1 && !level.isClientSide) {
            ItemStack finalResultStack = resultStack;
            predicate = (stack) -> ItemStack.isSameItemSameTags(stack, finalResultStack);

            do {
                if (!skipInventoryAmmo && standardAmmo.isEmpty()) {
                    standardAmmo = findMatchingAmmo(bow, living, predicate);
                }

                int needed = 1 - resultStack.getCount();
                Iterator var20 = tool.getModifierList().iterator();

                while(true) {
                    if (var20.hasNext()) {
                        ModifierEntry entry = (ModifierEntry)var20.next();
                        BowAmmoModifierHook hook = entry.getHook(ModifierHooks.BOW_AMMO);
                        ItemStack ammo = hook.findAmmo(tool, entry, living, standardAmmo, predicate);
                        if (ammo.isEmpty()) {
                            continue;
                        }

                        int gained = Math.min(needed, ammo.getCount());
                        hook.shrinkAmmo(tool, entry, living, ammo, gained);
                        resultStack.grow(gained);
                        break;
                    }

                    if (standardAmmo.isEmpty()) {
                        return resultStack;
                    }

                    if (needed <= standardAmmo.getCount()) {
                        standardAmmo.shrink(needed);
                        resultStack.grow(needed);
                        return resultStack;
                    }

                    resultStack.grow(standardAmmo.getCount());

                    standardAmmo = ItemStack.EMPTY;
                    break;
                }
            } while(resultStack.getCount() < 1);

            return resultStack;
        } else {
            return resultStack;
        }
    }

}
