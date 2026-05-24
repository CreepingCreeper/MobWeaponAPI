package dev.xkmc.mob_weapon_api.integration.cataclysm;

import com.github.L_Ender.cataclysm.init.ModItems;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.xkmc.mob_weapon_api.registry.WeaponRegistry;
import dev.xkmc.mob_weapon_api.registry.WeaponStatus;
import dev.xkmc.mob_weapon_api.util.DummyProjectileWeapon;
import net.minecraft.world.item.Items;

public class CataclysmIntegration {

	public static final ItemEntry<DummyProjectileWeapon> LASER_GATLING =
			DummyProjectileWeapon.create("laser_gatling", e -> e.is(Items.REDSTONE));

	public static void register() {

	}

	public static void init() {
		WeaponRegistry.BOW.register(ModItems.CURSED_BOW.getId(),
				e -> WeaponStatus.RANGED.of(e.is(ModItems.CURSED_BOW.get())),
				(golem, stack) -> new CursedBowBehavior(), 10
		);
		WeaponRegistry.BOW.register(ModItems.WRATH_OF_THE_DESERT.getId(),
				e -> WeaponStatus.RANGED.of(e.is(ModItems.WRATH_OF_THE_DESERT.get())),
				(golem, stack) -> new WrathBowBehavior(), 20
		);
		WeaponRegistry.BOW.register(ModItems.VOID_ASSULT_SHOULDER_WEAPON.getId(),
				e -> WeaponStatus.RANGED.of(e.is(ModItems.VOID_ASSULT_SHOULDER_WEAPON.get())),
				(golem, stack) -> new VoidHowitzerBehavior(), 100
		);
		WeaponRegistry.HOLD.register(ModItems.WITHER_ASSULT_SHOULDER_WEAPON.getId(),
				e -> WeaponStatus.RANGED.of(e.is(ModItems.WITHER_ASSULT_SHOULDER_WEAPON.get())),
				(golem, stack) -> new WitherMissileBehavior(), 20
		);
		WeaponRegistry.INSTANT.register(ModItems.LASER_GATLING.getId(),
				e -> WeaponStatus.RANGED.of(e.is(ModItems.LASER_GATLING.get())),
				(golem, stack) -> new LaserGatlingBehavior(), 10
		);
		WeaponRegistry.HOLD.register(ModItems.MEAT_SHREDDER.getId(),
				e -> WeaponStatus.MELEE.of(e.is(ModItems.MEAT_SHREDDER.get())),
				(golem, stack) -> new MeatShredderBehavior(), 50
		);
		WeaponRegistry.INSTANT.register(ModItems.SOUL_RENDER.getId(),
				e -> WeaponStatus.MELEE.of(e.is(ModItems.SOUL_RENDER.get())),
				(golem, stack) -> new SoulRenderBehavior(), 50
		);
		WeaponRegistry.INSTANT.register(ModItems.ANCIENT_SPEAR.getId(),
				e -> WeaponStatus.OFFENSIVE.of(e.is(ModItems.ANCIENT_SPEAR.get())),
				(golem, stack) -> new AncientSpearBehavior(), 10
		);
		WeaponRegistry.HOLD.register(ModItems.GAUNTLET_OF_MAELSTROM.getId(),
				e -> WeaponStatus.OFFENSIVE.of(e.is(ModItems.GAUNTLET_OF_MAELSTROM.get())),
				(golem, stack) -> new GauntletOfMaelstromBehavior(), 100
		);
		WeaponRegistry.INSTANT.register(ModItems.VOID_FORGE.getId(),
				e -> WeaponStatus.MELEE.of(e.is(ModItems.VOID_FORGE.get())),
				(golem, stack) -> new VoidForgeBehavior(), 70
		);
		WeaponRegistry.INSTANT.register(ModItems.INFERNAL_FORGE.getId(),
				e -> WeaponStatus.MELEE.of(e.is(ModItems.INFERNAL_FORGE.get())),
				(golem, stack) -> new InfernalForgeBehavior(), 70
		);
		WeaponRegistry.HOLD.register(ModItems.ASTRAPE.getId(),
				e -> WeaponStatus.OFFENSIVE.of(e.is(ModItems.ASTRAPE.get())),
				(golem, stack) -> new AstrapeBehavior(), 100
		);
		WeaponRegistry.HOLD.register(ModItems.CERAUNUS.getId(),
				e -> WeaponStatus.OFFENSIVE.of(e.is(ModItems.CERAUNUS.get())),
				(golem, stack) -> new CeraunusBehavior(), 100
		);
		WeaponRegistry.HOLD.register(ModItems.CORAL_SPEAR.getId(),
				e -> WeaponStatus.OFFENSIVE.of(e.is(ModItems.CORAL_SPEAR.get())),
				(golem, stack) -> new CoralSpearBehavior(), 10
		);
		try {
			WeaponRegistry.HOLD.register(ModItems.BRONTES.getId(),
					e -> WeaponStatus.OFFENSIVE.of(e.is(ModItems.BRONTES.get())),
					(golem, stack) -> new BrontesBehavior(), 10
			);
		} catch (NoSuchFieldError e) {
		}
	}

}
