package dev.xkmc.mob_weapon_api.integration.tinker;

import dev.xkmc.mob_weapon_api.init.MobWeaponAPI;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MonsterMeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;


public abstract class SyanModifier extends Modifier implements MeleeHitModifierHook, BlockBreakModifierHook, MonsterMeleeHitModifierHook.RedirectAfter {
    @Override
    protected void registerHooks(ModuleHookMap.@NotNull Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT, ModifierHooks.BLOCK_BREAK, ModifierHooks.MONSTER_MELEE_HIT);
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Nullable
    protected abstract ToolStack getOriginTool(LivingEntity living);

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        ToolDataNBT data = ((ToolStack)tool).getPersistentData();
        ToolStack originTool = getOriginTool(context.getAttacker());
        MobWeaponAPI.LOGGER.info(((ToolStack) tool).createStack() + "新工具");
        if (originTool != null) {
            originTool.getPersistentData().copyFrom(data.getCopy());
            MobWeaponAPI.LOGGER.info(originTool.createStack() + "原工具");
        }
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        ToolDataNBT data = ((ToolStack)tool).getPersistentData();
        ToolStack originTool = getOriginTool(context.getLiving());
        if (originTool != null) {
            originTool.getPersistentData().copyFrom(data.getCopy());
        }
    }
}
