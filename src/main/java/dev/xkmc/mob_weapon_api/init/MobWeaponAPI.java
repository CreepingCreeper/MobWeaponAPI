package dev.xkmc.mob_weapon_api.init;

import com.github.L_Ender.cataclysm.Cataclysm;
import com.mojang.logging.LogUtils;
import com.simibubi.create.Create;
import com.tterrag.registrate.Registrate;
import dev.xkmc.l2archery.init.L2Archery;
import dev.xkmc.l2complements.init.L2Complements;
import dev.xkmc.l2weaponry.init.L2Weaponry;
import dev.xkmc.mob_weapon_api.example.vanilla.VanillaMobManager;
import dev.xkmc.mob_weapon_api.integration.cataclysm.CataclysmIntegration;
import dev.xkmc.mob_weapon_api.integration.create.CreateIntegration;
import dev.xkmc.mob_weapon_api.integration.l2archery.L2ArcheryIntegration;
import dev.xkmc.mob_weapon_api.integration.l2complements.L2ComplementsIntegration;
import dev.xkmc.mob_weapon_api.integration.l2weaponry.L2WeaponryIntegration;
import dev.xkmc.mob_weapon_api.integration.tinker.TConstructIntegration;
import dev.xkmc.mob_weapon_api.integration.twilightforest.TFIntegration;
import dev.xkmc.mob_weapon_api.registry.WeaponRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import slimeknights.tconstruct.TConstruct;
import twilightforest.TwilightForestMod;

@Mod(MobWeaponAPI.MODID)
@Mod.EventBusSubscriber(modid = MobWeaponAPI.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MobWeaponAPI {

	public static final String MODID = "mob_weapon_api";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final Registrate REGISTRATE = Registrate.create(MODID);

	public MobWeaponAPI() {
        if (ModList.get().isLoaded(TConstruct.MOD_ID)) TConstructIntegration.register();
        if (ModList.get().isLoaded(Cataclysm.MODID)) CataclysmIntegration.register();
        if (ModList.get().isLoaded(TwilightForestMod.ID)) TFIntegration.register();
	}

	@SubscribeEvent
	public static void commonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			WeaponRegistry.init();
			if (ModList.get().isLoaded(L2Complements.MODID)) L2ComplementsIntegration.init();
			if (ModList.get().isLoaded(L2Archery.MODID)) L2ArcheryIntegration.init();
			if (ModList.get().isLoaded(L2Weaponry.MODID)) L2WeaponryIntegration.init();
			if (ModList.get().isLoaded(TConstruct.MOD_ID)) TConstructIntegration.init();
			if (ModList.get().isLoaded(Cataclysm.MODID)) CataclysmIntegration.init();
			if (ModList.get().isLoaded(TwilightForestMod.ID)) TFIntegration.init();
			if (ModList.get().isLoaded(Create.ID)) CreateIntegration.init();
			VanillaMobManager.init();
		});
	}

	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
	}

	public static ResourceLocation loc(String id) {
		return new ResourceLocation(MODID, id);
	}

}
