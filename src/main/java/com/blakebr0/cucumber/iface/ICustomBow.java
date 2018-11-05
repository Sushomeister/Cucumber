package com.blakebr0.cucumber.iface;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public interface ICustomBow {

	default float getDrawSpeedMulti(ItemStack stack) {
		return 1.0F;
	}

	default ItemStack findAmmo(EntityPlayer player) {
		if (this.isArrow(player.getHeldItem(EnumHand.OFF_HAND))) {
			return player.getHeldItem(EnumHand.OFF_HAND);
		} else if (this.isArrow(player.getHeldItem(EnumHand.MAIN_HAND))) {
			return player.getHeldItem(EnumHand.MAIN_HAND);
		} else {
			for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
				ItemStack stack = player.inventory.getStackInSlot(i);
				if (this.isArrow(stack)) {
					return stack;
				}
			}

			return ItemStack.EMPTY;
		}
	}

	default boolean isArrow(ItemStack stack) {
		return stack.getItem() instanceof ItemArrow;
	}
}
