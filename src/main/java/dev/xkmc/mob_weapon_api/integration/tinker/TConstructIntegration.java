package dev.xkmc.mob_weapon_api.integration.tinker;

import dev.xkmc.mob_weapon_api.init.MobWeaponAPI;
import dev.xkmc.mob_weapon_api.registry.WeaponRegistry;
import dev.xkmc.mob_weapon_api.registry.WeaponStatus;
import net.minecraft.tags.TagKey;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.modifiers.util.ModifierDeferredRegister;
import slimeknights.tconstruct.library.modifiers.util.StaticModifier;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableBowItem;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableCrossbowItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.data.ModifierIds;

public class TConstructIntegration {

    public static void register() {
        MODIFIERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

	public static void init() {
		WeaponRegistry.BOW.register(TConstruct.getResource("bow"),
				stack -> WeaponStatus.OFFENSIVE.of(stack.getItem() instanceof ModifiableBowItem && !ToolStack.from(stack).isBroken()),
				(golem, stack) -> new TinkerBowBehavior(), 10
		);
		WeaponRegistry.CROSSBOW.register(TConstruct.getResource("crossbow"),
				stack -> WeaponStatus.OFFENSIVE.of(stack.getItem() instanceof ModifiableCrossbowItem && !ToolStack.from(stack).isBroken()),
				(golem, stack) -> new TinkerCrossbowBehavior(), 10
		);
        WeaponRegistry.HOLD.register(TConstruct.getResource("throwing"),
                stack -> WeaponStatus.OFFENSIVE.of(stack.getItem() instanceof ModifiableItem && !ToolStack.from(stack).isBroken() && ToolStack.from(stack).getModifierLevel(ModifierIds.throwing) > 0),
                (golem, stack) -> new TinkerThrowingBehavior(), 10
        );
        WeaponRegistry.HOLD.register(TConstruct.getResource("spiting"),
                stack -> WeaponStatus.OFFENSIVE.of(stack.getItem() instanceof ModifiableItem && !ToolStack.from(stack).isBroken() && ToolStack.from(stack).getModifierLevel(TinkerModifiers.spitting.getId()) > 0),
                (golem, stack) -> new TinkerSpittingBehavior(), 10
        );

	}

    public static final TagKey<Modifier> THROWING_BLACKLIST = ModifierManager.getTag(MobWeaponAPI.loc("throwing_blacklist"));
    private static final ModifierDeferredRegister MODIFIERS = ModifierDeferredRegister.create(MobWeaponAPI.MODID);
    public static final StaticModifier<ThrowingSyanModifier> throwingSyan = MODIFIERS.register("throwing_syan", ThrowingSyanModifier::new);
    public static final StaticModifier<BallistaSyanModifier> ballistaSyan = MODIFIERS.register("ballista_syan", BallistaSyanModifier::new);

}
