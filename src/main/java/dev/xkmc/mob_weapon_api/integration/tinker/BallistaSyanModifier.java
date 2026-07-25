package dev.xkmc.mob_weapon_api.integration.tinker;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableBowItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;


public class BallistaSyanModifier extends SyanModifier {
    @Override
    @Nullable
    protected ToolStack getOriginTool(LivingEntity living) {
        ItemStack stack = living.getMainHandItem();
        if (!(stack.getItem() instanceof ModifiableBowItem bow)) return null;
        ToolStack tool = ToolStack.from(stack);
        if (tool.isBroken()) return null;
        for(ModifierEntry entry : tool.getModifierList()) {
            ItemStack ammo = entry.getHook(ModifierHooks.BOW_AMMO).findAmmo(tool, entry, living, ItemStack.EMPTY, bow.getSupportedBallistaAmmo());
            if (!ammo.isEmpty()) {
                return ToolStack.from(ammo);
            }
        }

        return null;
    }
}
