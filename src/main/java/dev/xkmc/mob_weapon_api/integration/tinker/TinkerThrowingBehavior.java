package dev.xkmc.mob_weapon_api.integration.tinker;

import dev.xkmc.mob_weapon_api.api.projectile.ProjectileWeaponUser;
import dev.xkmc.mob_weapon_api.api.simple.IHoldWeaponBehavior;
import dev.xkmc.mob_weapon_api.util.ShootUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.build.ConditionalStatModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.entity.ThrownTool;

public class TinkerThrowingBehavior implements IHoldWeaponBehavior {

    @Override
    public double range(LivingEntity user, ItemStack stack) {
        return 25;
    }

    @Override
    public int holdTime(LivingEntity user, ItemStack stack) {
        ToolStack tool = ToolStack.from(stack);
        float speed = ConditionalStatModifierHook.getModifiedStat(tool, user, ToolStats.DRAW_SPEED);
        if (tool.hasTag(TinkerTags.Items.MELEE_WEAPON)) {
            speed *= tool.getStats().get(ToolStats.ATTACK_SPEED);
        }
        return (int) Math.ceil(20.0F / speed);
    }

    // from ThrowingModule.onStopUsing
    @Override
    public int trigger(ProjectileWeaponUser user, ItemStack stack, LivingEntity target, int time) {
        user.user().releaseUsingItem();
        LivingEntity thrower = user.user();
        Level level = thrower.level();
        ToolStack tool = ToolStack.from(stack);
        ToolDamageUtil.damageAnimated(tool, 1, thrower);
        tool = tool.copy();

        for (ModifierEntry modifierEntry : tool.getModifiers().getModifiers()){
            Modifier modifier = modifierEntry.getModifier();
            if (modifier.is(TConstructIntegration.THROWING_BLACKLIST)) {
                tool.removeModifier(modifier.getId(), 999);
            }
        }
        tool.addModifier(TConstructIntegration.throwingSyan.getId(), 1);
        float velocity = ConditionalStatModifierHook.getModifiedStat(tool, thrower, ToolStats.VELOCITY);
        ThrownTool thrown = new ThrownTool(level, thrower, tool.createStack(), 1.0F, velocity, ConditionalStatModifierHook.getModifiedStat(tool, thrower, ToolStats.WATER_INERTIA));
        ShootUtils.shootAimHelper(target, thrown, velocity * 2.0F, 0.05F);

        thrown.onRelease(thrower, PersistentDataCapability.getOrWarn(thrown));
        level.addFreshEntity(thrown);
        level.playSound(null, thrown, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

        return 5;
    }

    @Override
    public void startHolding(LivingEntity user, ItemStack stack, InteractionHand hand){
        ToolStack tool = ToolStack.from(stack);
        GeneralInteractionModifierHook.startUsing(tool, ModifierIds.throwing, user, hand);
    }

}
