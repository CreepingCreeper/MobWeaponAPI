package dev.xkmc.mob_weapon_api.example.goal;

import dev.xkmc.mob_weapon_api.api.ai.IWeaponHolder;
import dev.xkmc.mob_weapon_api.api.goals.IMeleeGoal;
import dev.xkmc.mob_weapon_api.api.goals.IRangedWeaponGoal;
import dev.xkmc.mob_weapon_api.registry.WeaponStatus;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.Optional;

public abstract class SmartRangedAttackGoal<E extends Mob> extends Goal implements IRangedWeaponGoal<E> {

	protected final E mob;
	protected final IWeaponHolder holder;
	protected final IMeleeGoal melee;
	protected final double speedModifier;
	protected final double radius;
	protected int seeTime;
	private boolean strafingClockwise;
	private boolean strafingBackwards;
	private int strafingTime = -1;

	private int meleeTime = 0;

	protected SmartRangedAttackGoal(E mob, IWeaponHolder holder, IMeleeGoal melee, double speedModifier, double r) {
		this.mob = mob;
		this.holder = holder;
		this.melee = melee;
		this.speedModifier = speedModifier;
		this.radius = r;
		setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	public boolean canUse() {
		if (mob.getTarget() == null || !mob.getTarget().isAlive()) return false;
		ItemStack stack = mob.getItemInHand(holder.getWeaponHand());
		return mayActivate(stack);
	}

	public boolean canContinueToUse() {
		return canUse() || !mob.getNavigation().isDone();
	}

	public Optional<WeaponStatus> getWeaponStatus(ItemStack stack) {
		return Optional.empty();
	}

	public void start() {
		super.start();
		mob.setAggressive(true);
		var status = getWeaponStatus(mob.getItemInHand(holder.getWeaponHand()));
		holder.setInRangeAttack(status.isPresent() && status.get().isRanged());
	}

	public void stop() {
		super.stop();
		mob.setAggressive(false);
		holder.setInRangeAttack(false);
		seeTime = 5;
		meleeTime = 0;
		mob.stopUsingItem();
	}

	public boolean requiresUpdateEveryTick() {
		return true;
	}

	protected void doMelee() {
		if (meleeTime > 0) {
			meleeTime--;
			return;
		}
		var target = mob.getTarget();
		if (target == null) return;
		if (melee.canReachTarget(target)) {
			onMelee();
			this.mob.doHurtTarget(target);
			meleeTime = melee.getMeleeInterval();
		}
	}

	protected void onMelee() {
		this.mob.swing(InteractionHand.MAIN_HAND);
	}

	protected boolean meleeFinished() {
		return meleeTime <= 0;
	}

	@Override
	public double range(ItemStack stack) {
		return radius;
	}

	public final double attackRadiusSqr() {
		ItemStack stack = mob.getItemInHand(holder.getWeaponHand());
		double range = range(stack);
		var ins = mob.getAttribute(Attributes.FOLLOW_RANGE);
		if (ins != null) {
			range = Math.min(ins.getValue(), range);
		}
		return range * range;
	}

	protected void strafing() {
		var target = mob.getTarget();
		if (target == null) return;
		double dist = mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
		boolean sight = mob.getSensing().hasLineOfSight(target);
		boolean oldSight = seeTime > 0;
		if (sight != oldSight) {
			seeTime = 0;
		}
		if (sight) {
			++seeTime;
		} else {
			--seeTime;
		}
		double sqr = attackRadiusSqr();
		if (dist <= sqr && seeTime >= 5) {
			mob.getNavigation().stop();
			++strafingTime;
		} else {
			mob.getNavigation().moveTo(target, speedModifier);
			strafingTime = -1;
		}
		if (sqr < 15) {
			strafingTime = 0;
			mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
			return;
		}
		if (strafingTime >= 20) {
			if ((double) mob.getRandom().nextFloat() < 0.3D) {
				strafingClockwise = !strafingClockwise;
			}
			if ((double) mob.getRandom().nextFloat() < 0.3D) {
				strafingBackwards = !strafingBackwards;
			}
			strafingTime = 0;
		}
		if (strafingTime > -1) {
			if (dist > sqr * 0.75) {
				strafingBackwards = false;
			} else if (dist < sqr * 0.5) {
				strafingBackwards = true;
			}
			mob.getMoveControl().strafe(strafingBackwards ? -0.5F : 0.5F, strafingClockwise ? 0.5F : -0.5F);
			mob.lookAt(target, 30.0F, 30.0F);
		} else {
			mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
		}
	}

}
