package dev.xkmc.mob_weapon_api.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.UUID;

public class WeaponUtil {

	/**
	 * Returns the attack damage if user switch from ItemStack denoted by remove to ItemStack denoted by stack
	 */
	public static double getWeaponAttack(AttributeInstance attr, ItemStack stack, LivingEntity le, ItemStack remove) {
		double base = attr.getBaseValue();
		double add = 0, multBase = 1, multTotal = 1;
		LinkedHashMap<UUID, AttributeModifier> old = new LinkedHashMap<>();
		for (var mod : remove.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE)) {
			old.put(mod.getId(), mod);
		}
		for (var e : attr.getModifiers()) {
			if (old.containsKey(e.getId())) continue;
			switch (e.getOperation()) {
				case ADDITION -> add += e.getAmount();
				case MULTIPLY_BASE -> multBase += e.getAmount();
				case MULTIPLY_TOTAL -> multTotal *= 1 + e.getAmount();
			}
		}
		LinkedHashMap<UUID, AttributeModifier> item = new LinkedHashMap<>();
		for (var mod : remove.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE)) {
			item.put(mod.getId(), mod);
		}
		for (var e : item.values()) {
			switch (e.getOperation()) {
				case ADDITION -> add += e.getAmount();
				case MULTIPLY_BASE -> multBase += e.getAmount();
				case MULTIPLY_TOTAL -> multTotal *= 1 + e.getAmount();
			}
		}
		return (base + add) * multBase * multTotal;
	}

	public static boolean isBetterWeapon(LivingEntity le, ItemStack stack, ItemStack prev) {
		var attr = le.getAttribute(Attributes.ATTACK_DAMAGE);
		if (attr == null) return false;
		var current = attr.getValue();
		var next = WeaponUtil.getWeaponAttack(attr, stack, le, prev);
		return next > current;
	}


}
