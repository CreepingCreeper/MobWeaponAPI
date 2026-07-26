package dev.xkmc.mob_weapon_api.integration.tinker;

import dev.xkmc.mob_weapon_api.api.projectile.BowUseContext;
import dev.xkmc.mob_weapon_api.api.projectile.IBowBehavior;
import dev.xkmc.mob_weapon_api.api.projectile.ProjectileWeaponUser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ConditionalStatModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.ranged.BowAmmoModifierHook;
import slimeknights.tconstruct.library.tools.capability.EntityModifierCapability;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableBowItem;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableLauncherItem;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.tools.entity.ThrownTool;

import static slimeknights.tconstruct.library.tools.item.ranged.ModifiableBowItem.KEY_BALLISTA;
import static slimeknights.tconstruct.library.tools.item.ranged.ModifiableLauncherItem.KEY_DRAWBACK_AMMO;

public class TinkerBowBehavior implements IBowBehavior {

	@Override
	public float getPowerForTime(BowUseContext user, ItemStack stack, int pullTime) {
		return 1;
	}

	@Override
	public int getStandardPullTime(BowUseContext user, ItemStack stack) {
		return (int) Math.ceil(20 / ConditionalStatModifierHook.getModifiedStat(ToolStack.from(stack), user.user(), ToolStats.DRAW_SPEED));
	}

	@Override
	public boolean hasProjectile(ProjectileWeaponUser user, ItemStack stack) {
		if (!(stack.getItem() instanceof ModifiableBowItem bow)) return false;
		ToolStack tool = ToolStack.from(stack);
        if (tool.isBroken()) return false;
        return !BowAmmoModifierHook.getAmmo(tool, stack, user.user(), ModifiableBowItem.isBallista(tool) ? bow.getSupportedBallistaAmmo() : bow.getSupportedHeldProjectiles()).isEmpty();
    }

    @Override
	public int shootArrow(BowUseContext user, float dist, ItemStack stack, InteractionHand hand) {
		if (!(stack.getItem() instanceof ModifiableBowItem bow)) return 20;
		shoot(bow, stack, user);
		return 10;
	}

	// from ModifiableBowItem.releaseUsing
    public void shoot(ModifiableBowItem bowItem, ItemStack bowStack, BowUseContext strategy) {
        var user = strategy.user();
        var level = user.level();
        ToolStack tool = ToolStack.from(bowStack);
        if (!hasProjectile(strategy, bowStack)) return;
        float velocity = strategy.getInitialVelocityFactor() * ConditionalStatModifierHook.getModifiedStat(tool, user, ToolStats.VELOCITY);
        if (level.isClientSide) return;
        boolean isBallista = ModifiableBowItem.isBallista(tool);
        ItemStack ammo = GolemTinkerAmmoHook.consumeAmmo(tool, bowStack, user, isBallista, isBallista ? bowItem.getSupportedBallistaAmmo() : bowItem.getSupportedHeldProjectiles());
        if (ammo.isEmpty()) {
            ammo = new ItemStack(Items.ARROW);
        }
        float startAngle = ModifiableLauncherItem.getAngleStart(ammo.getCount());
        int primaryIndex = ammo.getCount() / 2;
        float inaccuracy = ModifierUtil.getInaccuracy(tool, user) * strategy.getInitialInaccuracy();

        // custom shoot direction
        var origin = user.getEyePosition().add(0, -0.1, 0);
        var consumer = strategy.aim(origin, velocity, 0.05f, inaccuracy);
        SoundEvent sound = SoundEvents.ARROW_SHOOT;
        boolean thrownTool = ammo.is(TinkerTags.Items.BALLISTA_AMMO);
        ArrowItem arrowItem = null;
        float waterInertia = 0.6F;
        ToolStack thrown = null;
        if (thrownTool){
            sound = SoundEvents.TRIDENT_THROW;
            thrown = ToolStack.from(ammo);
            float thrownVelocity = ConditionalStatModifierHook.getModifiedStat(thrown, user, ToolStats.VELOCITY);
            velocity *= thrownVelocity * ConditionalStatModifierHook.getModifiedStat(thrown, user, ToolStats.DRAW_SPEED) / 1.5F;
            if (ammo.is(TinkerTags.Items.MELEE_WEAPON)) {
                velocity *= thrown.getStats().get(ToolStats.ATTACK_SPEED);
            }

            velocity *= thrownVelocity;
            waterInertia = ConditionalStatModifierHook.getModifiedStat(thrown, user, ToolStats.WATER_INERTIA);
        }else arrowItem = (ArrowItem) ammo.getItem();

        for (int i = 0; i < ammo.getCount(); ++i) {
            AbstractArrow arrow;
            if (thrownTool) {
                for (ModifierEntry modifierEntry : thrown.getModifiers().getModifiers()){
                    Modifier modifier = modifierEntry.getModifier();
                    if (modifier.is(TConstructIntegration.THROWING_BLACKLIST)) {
                        thrown.removeModifier(modifier.getId(), 999);
                    }
                }
                thrown.addModifier(TConstructIntegration.ballistaSyan.getId(), 1);
                ThrownTool thrownPrj = new ThrownTool(level, user, thrown.createStack(), 1.0F, velocity, waterInertia);
                thrownPrj.setOriginalSlot(-1);
                arrow = thrownPrj;
            } else {
                arrow = arrowItem.createArrow(level, ammo, user);
                arrow.pickup = AbstractArrow.Pickup.ALLOWED;
            }
            float angle = startAngle + (float) (10 * i);
            consumer.shoot(arrow, angle);
            arrow.setCritArrow(true);
            float baseDmg = (float) (arrow.getBaseDamage() - 2.0 + tool.getStats().get(ToolStats.PROJECTILE_DAMAGE));
            arrow.setBaseDamage(ConditionalStatModifierHook.getModifiedStat(tool, user, ToolStats.PROJECTILE_DAMAGE, baseDmg));
            ModifierNBT modifiers = tool.getModifiers();
            arrow.getCapability(EntityModifierCapability.CAPABILITY).ifPresent((cap) -> cap.setModifiers(modifiers));
            ModDataNBT arrowData = PersistentDataCapability.getOrWarn(arrow);

            for (ModifierEntry entry : modifiers.getModifiers()) {
                entry.getHook(ModifierHooks.PROJECTILE_LAUNCH).onProjectileLaunch(tool, entry, user, ammo, arrow, arrow, arrowData, i == primaryIndex);
            }
            level.addFreshEntity(arrow);
            level.playSound(null, user.getX(), user.getY(), user.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F + angle / 10.0F);
        }
        ToolDamageUtil.damageAnimated(tool, ammo.getCount(), user, user.getUsedItemHand());
    }


    // from ModifiableBowItem.use
    public void startUsingBow(ProjectileWeaponUser user, ItemStack stack) {
        ToolStack tool = ToolStack.from(stack);
        LivingEntity shooter = user.user();
        GeneralInteractionModifierHook.startDrawtime(tool, shooter, 1.0F);
        boolean isBallista = ModifiableBowItem.isBallista(tool);
        ModifiableBowItem bow = (ModifiableBowItem) stack.getItem();
        ItemStack ammo = BowAmmoModifierHook.getAmmo(tool, stack, shooter, isBallista ? bow.getSupportedBallistaAmmo() : bow.getSupportedHeldProjectiles());
        if (!ammo.isEmpty()) {
            tool.getPersistentData().put(KEY_DRAWBACK_AMMO, ammo.save(new CompoundTag()));

            if (isBallista) {
                int flag;
                if (!ammo.is(TinkerTags.Items.BALLISTA_AMMO)) {
                    flag = 3;
                } else {
                    flag = ammo != shooter.getMainHandItem() && ammo != shooter.getOffhandItem() ? 2 : 1;
                }

                tool.getPersistentData().putInt(KEY_BALLISTA, flag);
            }
        }
        shooter.level().playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), Sounds.LONGBOW_CHARGE.getSound(), SoundSource.PLAYERS, 0.75F, 1.0F);
    }
}
