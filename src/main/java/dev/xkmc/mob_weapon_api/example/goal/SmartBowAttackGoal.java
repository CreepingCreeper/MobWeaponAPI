package dev.xkmc.mob_weapon_api.example.goal;

import dev.xkmc.mob_weapon_api.api.ai.IWeaponHolder;
import dev.xkmc.mob_weapon_api.api.goals.IMeleeGoal;
import dev.xkmc.mob_weapon_api.registry.WeaponRegistry;
import dev.xkmc.mob_weapon_api.registry.WeaponStatus;
import dev.xkmc.mob_weapon_api.util.WeaponUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class SmartBowAttackGoal<E extends Mob> extends SmartRangedAttackGoal<E> {

	private int attackTime = -1;

	public SmartBowAttackGoal(E mob, IMeleeGoal melee, double speed, double radius) {
		super(mob, mob instanceof IWeaponHolder h ? h : IWeaponHolder.simple(mob), melee, speed, radius);
	}

	public SmartBowAttackGoal(E mob, IWeaponHolder holder, IMeleeGoal melee, double speed, double radius) {
		super(mob, holder, melee, speed, radius);
	}

	@Override
	public boolean mayActivate(ItemStack stack) {
		var weapon = WeaponRegistry.BOW.get(mob, stack);
        return weapon.map(iBowBehavior -> iBowBehavior.hasProjectile(holder.toUser(), stack)).orElse(false);
    }

	@Override
	public Optional<WeaponStatus> getWeaponStatus(ItemStack stack) {
		return WeaponRegistry.BOW.getProperties(stack);
	}

	@Override
	public boolean shouldUseForMelee(ItemStack other) {
		return !WeaponUtil.isBetterWeapon(mob, other, mob.getMainHandItem());
	}

	@Override
	public void stop() {
		attackTime = -1;
		super.stop();
	}

	public void tick() {
		doMelee();
		strafing();
		LivingEntity target = mob.getTarget();
		var hand = holder.getWeaponHand();
		ItemStack stack = mob.getItemInHand(hand);
		var weapon = WeaponRegistry.BOW.get(mob, stack);
		if (weapon.isEmpty()) return;
		if (mob.isUsingItem() && target != null) {
			var user = holder.toUser();
			double dist = mob.distanceTo(target);
			if (seeTime < -60) {
				mob.stopUsingItem();
			} else if (seeTime > 0) {
				int i = mob.getTicksUsingItem();
				int pullTime = weapon.get().getPreferredPullTime(user, stack, dist);
				if (i >= pullTime) {
					performRangedAttack(target, weapon.get().getPowerForTime(user, stack, i), stack, hand);
					mob.stopUsingItem();
				} else {
					weapon.get().tickUsingBow(user, stack);
				}
			}
		} else if (--attackTime <= 0 && seeTime >= -60) {
			var user = holder.toUser();
			if (target != null) {
				double dist = mob.distanceTo(target);
				int pullTime = weapon.get().getPreferredPullTime(user, stack, dist);
				if (pullTime <= 0) {
					performRangedAttack(target, 0, stack, hand);
					return;
				}
			}
			mob.startUsingItem(hand);
			weapon.ifPresent(e -> e.startUsingBow(user, stack));

		}
	}

	@Override
	public void performRangedAttack(LivingEntity target, float power, ItemStack stack, InteractionHand hand) {
		var opt = WeaponRegistry.BOW.get(mob, stack);
		if (opt.isEmpty()) return;
		attackTime = opt.get().shootArrow(holder.toUser(), power, stack, hand);
	}

}
