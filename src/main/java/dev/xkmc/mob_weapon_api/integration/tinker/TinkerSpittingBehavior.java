package dev.xkmc.mob_weapon_api.integration.tinker;

import dev.xkmc.mob_weapon_api.api.projectile.ProjectileWeaponUser;
import dev.xkmc.mob_weapon_api.api.simple.IHoldWeaponBehavior;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectManager;
import slimeknights.tconstruct.library.modifiers.hook.build.ConditionalStatModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.tools.capability.fluid.ToolTankHelper;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.tools.TinkerModifiers;

public class TinkerSpittingBehavior implements IHoldWeaponBehavior {

    @Override
    public double range(LivingEntity user, ItemStack stack) {
        return 25;
    }

    @Override
    public int holdTime(LivingEntity user, ItemStack stack) {
        ToolStack tool = ToolStack.from(stack);
        float speed = ConditionalStatModifierHook.getModifiedStat(tool, user, ToolStats.DRAW_SPEED);
        return (int) Math.ceil(20.0F / speed);
    }

    @Override
    public int trigger(ProjectileWeaponUser user, ItemStack stack, LivingEntity target, int time) {
        user.user().releaseUsingItem();
        return 5;
    }

    @Override
    public void startHolding(LivingEntity user, ItemStack stack, InteractionHand hand){
        ToolStack tool = ToolStack.from(stack);
        GeneralInteractionModifierHook.startUsingWithDrawtime(tool, TinkerModifiers.spitting.getId(), user, hand, 1.5F);
    }

    @Override
    public boolean isValid(ProjectileWeaponUser user, ItemStack stack) {
        ToolStack tool = ToolStack.from(stack);
        FluidStack fluid = ToolTankHelper.TANK_HELPER.getFluid(tool);
        return fluid.getAmount() >= tool.getModifierLevel(TinkerModifiers.spitting.getId()) && FluidEffectManager.INSTANCE.find(fluid.getFluid()).hasEffects();
    }
}
