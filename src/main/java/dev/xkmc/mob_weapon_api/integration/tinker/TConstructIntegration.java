package dev.xkmc.mob_weapon_api.integration.tinker;

import dev.xkmc.mob_weapon_api.init.MobWeaponAPI;
import dev.xkmc.mob_weapon_api.registry.WeaponRegistry;
import dev.xkmc.mob_weapon_api.registry.WeaponStatus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableBowItem;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableCrossbowItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.data.ModifierIds;

public class TConstructIntegration {

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

    public static final TagKey<Modifier> THROWING_BLACKLIST = ModifierManager.getTag(new ResourceLocation(MobWeaponAPI.MODID, "throwing_blacklist"));

}
