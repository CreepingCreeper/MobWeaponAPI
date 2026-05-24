package dev.xkmc.mob_weapon_api.integration.cataclysm;

import dev.xkmc.cataclysm_mux.MWCataProxy;
import dev.xkmc.mob_weapon_api.api.projectile.ProjectileWeaponUser;
import dev.xkmc.mob_weapon_api.example.behavior.ThrowableBehavior;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BrontesBehavior extends ThrowableBehavior {

	@Override
	public double range(LivingEntity user, ItemStack stack) {
		return 20;
	}

	@Override
	public float getSpeed(ItemStack stack, Projectile proj) {
		return 2.5f;
	}

	@Override
	public float getGravity(ItemStack stack, Projectile proj) {
		return 0.1f;
	}

	@Override
	protected @Nullable Projectile getProjectile(ProjectileWeaponUser user, ItemStack stack, LivingEntity target, int time) {
		return MWCataProxy.brontes(user.user(), stack, target);
	}

}
