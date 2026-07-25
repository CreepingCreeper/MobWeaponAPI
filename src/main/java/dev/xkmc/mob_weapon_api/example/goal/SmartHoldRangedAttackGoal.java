package dev.xkmc.mob_weapon_api.example.goal;

import dev.xkmc.mob_weapon_api.api.ai.IWeaponHolder;
import dev.xkmc.mob_weapon_api.api.goals.IMeleeGoal;
import dev.xkmc.mob_weapon_api.api.simple.IHoldWeaponBehavior;
import dev.xkmc.mob_weapon_api.registry.WeaponRegistry;
import dev.xkmc.mob_weapon_api.registry.WeaponStatus;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class SmartHoldRangedAttackGoal<E extends Mob> extends SmartRangedAttackGoal<E> {

	private int attackTime = -1;

	public SmartHoldRangedAttackGoal(E mob, IMeleeGoal melee, double speed) {
		super(mob, mob instanceof IWeaponHolder h ? h : IWeaponHolder.simple(mob), melee, speed, 0);
	}

	public SmartHoldRangedAttackGoal(E mob, IWeaponHolder holder, IMeleeGoal melee, double speed, double radius) {
		super(mob, holder, melee, speed, radius);
	}

	public Optional<IHoldWeaponBehavior> get(ItemStack stack) {
		return WeaponRegistry.HOLD.get(mob, stack);
	}

	@Override
	public Optional<WeaponStatus> getWeaponStatus(ItemStack stack) {
		return WeaponRegistry.HOLD.getProperties(stack);
	}

	@Override
	public boolean mayActivate(ItemStack stack) {
		var weapon = get(stack);
		return weapon.isPresent() && weapon.get().isValid(holder.toUser(), stack);
	}

	@Override
	public void stop() {
		attackTime = -1;
		super.stop();
	}

	@Override
	public double range(ItemStack stack) {
		var weapon = get(stack);
		return weapon.map(b -> b.range(mob, stack)).orElse(0.0);
	}

	@Override
	protected void onMelee() {
		if (mob.isUsingItem()) {
			mob.stopUsingItem();
		}
		super.onMelee();
	}

	public void tick() {
		ItemStack stack = mob.getItemInHand(holder.getWeaponHand());
		var weapon = get(stack);
		if (weapon.isEmpty()) return;
		if (weapon.get().canMelee()) doMelee();
		strafing();
		LivingEntity target = mob.getTarget();
		boolean invalidTarget = target == null || !target.isAlive();
		boolean withInRange = !invalidTarget && mob.distanceTo(target) < weapon.get().range(mob, stack);
		var user = holder.toUser();
		if (mob.isUsingItem()) {
			boolean infiniteUse = weapon.get().infiniteUse(mob, stack);
			if (seeTime < -60 || infiniteUse && !withInRange) {
				mob.stopUsingItem();
			} else if (seeTime > 0 && !invalidTarget) {
				int i = mob.getTicksUsingItem();
				if (!infiniteUse && i >= weapon.get().holdTime(mob, stack)) {
					attackTime = weapon.get().trigger(user, stack, target, i);
					mob.stopUsingItem();
				} else {
					weapon.get().tickUsing(user, stack, i);
				}
			}
		} else if (meleeFinished() && --attackTime <= 0 && seeTime >= -60 && withInRange) {
			mob.startUsingItem(holder.getWeaponHand());
            weapon.get().startHolding(user.user(), stack, holder.getWeaponHand());
		}
	}

	@Override
	public void performRangedAttack(LivingEntity target, float power, ItemStack stack, InteractionHand hand) {
	}

}
