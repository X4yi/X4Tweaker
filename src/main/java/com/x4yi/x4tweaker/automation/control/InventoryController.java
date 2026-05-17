package com.x4yi.x4tweaker.automation.control;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

public class InventoryController {
    private final Minecraft mc = Minecraft.getMinecraft();
    private int previousSlot = -1;

    public boolean selectFoodFromHotbar() {
        EntityPlayerSP player = mc.player;
        if (player == null) return false;

        int currentSlot = player.inventory.currentItem;
        if (currentSlot < 0 || currentSlot >= 9) return false;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof ItemFood || stack.getItem() == Items.GOLDEN_APPLE) {
                    if (previousSlot < 0) {
                        previousSlot = currentSlot;
                    }
                    player.inventory.currentItem = i;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasFoodInHotbar() {
        EntityPlayerSP player = mc.player;
        if (player == null) return false;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof ItemFood || stack.getItem() == Items.GOLDEN_APPLE) {
                    return true;
                }
            }
        }
        return false;
    }

    public int countFoodInHotbar() {
        EntityPlayerSP player = mc.player;
        if (player == null) return 0;

        int count = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof ItemFood || stack.getItem() == Items.GOLDEN_APPLE) {
                    count += stack.getCount();
                }
            }
        }
        return count;
    }

    public void restoreSlot() {
        EntityPlayerSP player = mc.player;
        if (player != null && previousSlot >= 0 && previousSlot < 9) {
            player.inventory.currentItem = previousSlot;
        }
        previousSlot = -1;
    }
}
