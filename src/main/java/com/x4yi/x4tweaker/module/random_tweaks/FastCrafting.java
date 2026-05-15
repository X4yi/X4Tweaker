package com.x4yi.x4tweaker.module.random_tweaks;

import com.x4yi.x4tweaker.module.Category;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.setting.NumberSetting;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class FastCrafting extends Module {
    private final ItemStack[] lastRecipe = new ItemStack[9];
    private boolean wasSpaceDown = false;
    private int refillTickTimer = 0;

    private final Deque<Runnable> actionQueue = new ArrayDeque<>();
    private final NumberSetting delayTicks = new NumberSetting("Click Delay", "Ticks entre cada click de inventario", 1.0, 0.0, 10.0, 1.0);

    public FastCrafting() {
        super("FastCrafting", "Rellena automáticamente la mesa de crafteo con la última receta", Category.TWEAKS);
        addSetting(delayTicks);
        for (int i = 0; i < 9; i++) lastRecipe[i] = ItemStack.EMPTY;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || !(mc.currentScreen instanceof GuiCrafting)) {
            wasSpaceDown = false;
            actionQueue.clear();
            return;
        }

        ContainerWorkbench container = (ContainerWorkbench) mc.player.openContainer;

        boolean isEmpty = true;
        for (int i = 1; i <= 9; i++) {
            if (container.inventorySlots.get(i).getHasStack()) {
                isEmpty = false;
                break;
            }
        }

        if (!isEmpty) {
            for (int i = 1; i <= 9; i++) {
                Slot slot = container.inventorySlots.get(i);
                lastRecipe[i - 1] = (slot != null && slot.getHasStack()) ? slot.getStack().copy() : ItemStack.EMPTY;
            }
        }

        boolean isSpaceDown = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
        if (isSpaceDown && !wasSpaceDown && actionQueue.isEmpty()) {
            buildActionQueue(container);
        }
        wasSpaceDown = isSpaceDown;

        if (!actionQueue.isEmpty()) {
            int delay = delayTicks.getValue().intValue();
            if (delay <= 0) {
                runActions(3);
            } else {
                refillTickTimer++;
                if (refillTickTimer >= delay) {
                    refillTickTimer = 0;
                    runActions(1);
                }
            }
        }
    }

    private void runActions(int count) {
        for (int i = 0; i < count && !actionQueue.isEmpty(); i++) {
            actionQueue.poll().run();
        }
    }

    private void buildActionQueue(ContainerWorkbench container) {
        int craftsToFill = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? calculateMaxCrafts(container) : 1;
        for (int slot = 1; slot <= 9; slot++) {
            ItemStack needed = lastRecipe[slot - 1];
            if (needed.isEmpty()) continue;
            if (!container.inventorySlots.get(slot).getHasStack()) {
                queueActionsForSlot(container, slot, needed, craftsToFill);
            }
        }
    }

    private void queueActionsForSlot(ContainerWorkbench container, int targetSlot, ItemStack needed, int amount) {
        int remaining = amount;
        int invSize = container.inventorySlots.size();
        for (int i = 10; i < invSize && remaining > 0; i++) {
            Slot invSlot = container.inventorySlots.get(i);
            if (invSlot == null || !invSlot.getHasStack()) continue;

            ItemStack stack = invSlot.getStack();
            if (stack.getItem() != needed.getItem() || stack.getMetadata() != needed.getMetadata()) continue;
            if ((stack.hasTagCompound() || needed.hasTagCompound()) && !ItemStack.areItemStackTagsEqual(stack, needed)) continue;

            final int fromSlot = i;
            int take = Math.min(remaining, stack.getCount());

            actionQueue.add(() -> mc.playerController.windowClick(container.windowId, fromSlot, 0, ClickType.PICKUP, mc.player));
            for (int c = 0; c < take; c++) {
                actionQueue.add(() -> mc.playerController.windowClick(container.windowId, targetSlot, 1, ClickType.PICKUP, mc.player));
            }
            actionQueue.add(() -> mc.playerController.windowClick(container.windowId, fromSlot, 0, ClickType.PICKUP, mc.player));

            remaining -= take;
        }
    }

    private int calculateMaxCrafts(ContainerWorkbench container) {
        Map<String, Integer> neededPerCraft = new HashMap<>();
        for (int i = 0; i < 9; i++) {
            ItemStack s = lastRecipe[i];
            if (s.isEmpty()) continue;
            String key = buildItemKey(s);
            neededPerCraft.put(key, neededPerCraft.getOrDefault(key, 0) + 1);
        }
        if (neededPerCraft.isEmpty()) return 1;

        Map<String, Integer> available = new HashMap<>();
        int invSize = container.inventorySlots.size();
        for (int i = 10; i < invSize; i++) {
            Slot slot = container.inventorySlots.get(i);
            if (slot == null || !slot.getHasStack()) continue;
            ItemStack s = slot.getStack();
            String key = buildItemKey(s);
            if (neededPerCraft.containsKey(key)) {
                available.put(key, available.getOrDefault(key, 0) + s.getCount());
            }
        }

        int maxCrafts = 64;
        for (Map.Entry<String, Integer> entry : neededPerCraft.entrySet()) {
            int nav = available.getOrDefault(entry.getKey(), 0);
            int crafts = nav / entry.getValue();
            if (crafts < maxCrafts) maxCrafts = crafts;
        }
        return Math.max(1, maxCrafts);
    }

    private String buildItemKey(ItemStack s) {
        return s.getItem().getRegistryName() + ":" + s.getMetadata() + ":" +
               (s.hasTagCompound() ? s.getTagCompound().toString() : "");
    }
}
