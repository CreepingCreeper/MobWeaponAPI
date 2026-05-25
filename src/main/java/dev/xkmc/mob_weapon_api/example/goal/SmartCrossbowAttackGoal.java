package dev.xkmc.mob_weapon_api.example.goal;

import dev.xkmc.mob_weapon_api.api.ai.IWeaponHolder;
import dev.xkmc.mob_weapon_api.api.goals.IMeleeGoal;
import dev.xkmc.mob_weapon_api.registry.WeaponRegistry;
import dev.xkmc.mob_weapon_api.registry.WeaponStatus;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class SmartCrossbowAttackGoal<E extends Mob> extends SmartRangedAttackGoal<E> {
	private CrossbowState crossbowState = CrossbowState.UNCHARGED;
	private int attackDelay;

	public SmartCrossbowAttackGoal(E mob, IMeleeGoal melee, double speed, float radius) {
		super(mob, mob instanceof IWeaponHolder h ? h : IWeaponHolder.simple(mob), melee, speed, radius);
	}

	public SmartCrossbowAttackGoal(E mob, IWeaponHolder holder, IMeleeGoal melee, double speed, double radius) {
		super(mob, holder, melee, speed, radius);
	}

	@Override
	public boolean mayActivate(ItemStack stack) {
		var weapon = WeaponRegistry.CROSSBOW.get(mob, stack);
		if (weapon.isEmpty()) return false;
		return weapon.get().hasProjectile(holder.toUser(), stack) ||
				weapon.get().hasLoadedProjectile(mob, stack);
	}

	@Override
	public Optional<WeaponStatus> getWeaponStatus(ItemStack stack) {
		return WeaponRegistry.CROSSBOW.getProperties(stack);
	}

	public void setChargingCrossbow(boolean bool) {
		if (mob instanceof CrossbowAttackMob e) e.setChargingCrossbow(bool);
	}

	public void stop() {
		super.stop();
		setChargingCrossbow(false);
		attackDelay = 0;
	}

	public void tick() {
		doMelee();
		strafing();
		LivingEntity target = mob.getTarget();
		var hand = holder.getWeaponHand();
		ItemStack stack = mob.getItemInHand(hand);
		var weapon = WeaponRegistry.CROSSBOW.get(mob, stack);
		if (weapon.isEmpty()) return;
		var user = holder.toUser();
		var behavior = weapon.get();
		if (crossbowState == CrossbowState.UNCHARGED) {
			if (behavior.hasLoadedProjectile(mob, stack)) {
				crossbowState = CrossbowState.CHARGED;
				setChargingCrossbow(false);
			} else if (behavior.hasProjectile(user, stack)) {
				if (behavior.chargeTime(mob, stack) <= 0) {
					if (weapon.get().tryCharge(user, stack)) {
						crossbowState = CrossbowState.CHARGED;
					}
				} else {
					mob.startUsingItem(holder.getWeaponHand());
					crossbowState = CrossbowState.CHARGING;
					setChargingCrossbow(true);
				}
			}
		} else if (crossbowState == CrossbowState.CHARGING) {
			if (!mob.isUsingItem()) {
				crossbowState = CrossbowState.UNCHARGED;
			}
			if (mob.getTicksUsingItem() >= weapon.get().chargeTime(mob, stack)) {
				mob.releaseUsingItem();
				if (weapon.get().tryCharge(user, stack)) {
					crossbowState = CrossbowState.CHARGED;
					setChargingCrossbow(false);
				}
			}
		}

		if (target != null) {
			if (crossbowState == CrossbowState.CHARGED) {
				if (attackDelay == 0) {
					attackDelay = 10;
				}
				--attackDelay;
				if (attackDelay == 0) {
					crossbowState = CrossbowState.READY_TO_ATTACK;
				}
			} else if (crossbowState == CrossbowState.READY_TO_ATTACK && seeTime > 0) {
				performRangedAttack(target, 1.0F, stack, hand);
				behavior.release(stack);
				crossbowState = CrossbowState.UNCHARGED;
			}
		}
	}

	@Override
	public void performRangedAttack(LivingEntity target, float power, ItemStack stack, InteractionHand hand) {
		WeaponRegistry.CROSSBOW.get(mob, stack).ifPresent(e -> e.performRangedAttack(holder.toUser(), stack, hand));
	}

	enum CrossbowState {
		UNCHARGED,
		CHARGING,
		CHARGED,
		READY_TO_ATTACK
	}

}
