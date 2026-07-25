package dev.xkmc.mob_weapon_api.integration.tinker;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;


public class ThrowingSyanModifier extends SyanModifier {
    @Override
    @Nullable
    protected ToolStack getOriginTool(LivingEntity living) {
        ItemStack stack = living.getMainHandItem();
        if (!(stack.getItem() instanceof ModifiableItem)) return null;
        ToolStack tool = ToolStack.from(stack);
        if (tool.isBroken()) return null;
        return tool;
    }
}
