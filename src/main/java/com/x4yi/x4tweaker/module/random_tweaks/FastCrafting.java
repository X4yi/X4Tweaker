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

import java.util.HashMap;
import java.util.Map;

public class FastCrafting extends Module {
    private static final int GRID_START = 1;
    private static final int GRID_END = 9;
    private static final int RESULT_SLOT = 0;
    private static final int INV_START = 10;

    private final ItemStack[] lastRecipe = new ItemStack[9];
    private boolean triggerPressed = false;

    private final NumberSetting triggerKey = new NumberSetting("Trigger Key", "Tecla para activar FastCrafting", Keyboard.KEY_SPACE, 1D, 255D, 1D);

    public FastCrafting() {
        super("FastCrafting", "Rellena y craftea instantáneamente usando la receta detectada", Category.TWEAKS);
        addSetting(triggerKey);
        for (int i = 0; i < lastRecipe.length; i++) {
            lastRecipe[i] = ItemStack.EMPTY;
        }
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || !(mc.currentScreen instanceof GuiCrafting) || !(mc.player.openContainer instanceof ContainerWorkbench)) {
            triggerPressed = false;
            return;
        }

        ContainerWorkbench container = (ContainerWorkbench) mc.player.openContainer;
        captureRecipeSnapshot(container);

        int key = triggerKey.getValue().intValue();
        boolean down = Keyboard.isKeyDown(key);
        if (!down) {
            triggerPressed = false;
            return;
        }
        if (triggerPressed) return;
        triggerPressed = true;

        boolean massCraft = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        executeCraft(container, massCraft);
    }

    private void captureRecipeSnapshot(ContainerWorkbench container) {
        boolean hasGridItems = false;
        for (int slot = GRID_START; slot <= GRID_END; slot++) {
            Slot s = container.inventorySlots.get(slot);
            if (s != null && s.getHasStack()) {
                hasGridItems = true;
                break;
            }
        }
        if (!hasGridItems) return;

        for (int slot = GRID_START; slot <= GRID_END; slot++) {
            Slot s = container.inventorySlots.get(slot);
            lastRecipe[slot - GRID_START] = (s == null || !s.getHasStack()) ? ItemStack.EMPTY : s.getStack().copy();
        }
    }

    private void executeCraft(ContainerWorkbench container, boolean massCraft) {
        if (!hasRecipe()) return;
        if (!clearCursorStack(container)) return;

        int loops = massCraft ? Math.max(1, calculateMaxCrafts(container)) : 1;
        for (int i = 0; i < loops; i++) {
            if (!isContainerStillValid(container)) return;
            if (!fillGridForOneCraft(container)) return;
            if (!takeCraftResult(container)) return;
        }
    }

    private boolean fillGridForOneCraft(ContainerWorkbench container) {
        for (int gridSlot = GRID_START; gridSlot <= GRID_END; gridSlot++) {
            Slot slot = container.inventorySlots.get(gridSlot);
            if (slot == null) return false;
            if (slot.getHasStack()) continue;

            ItemStack needed = lastRecipe[gridSlot - GRID_START];
            if (needed.isEmpty()) continue;

            int invSlot = findMatchingInventorySlot(container, needed);
            if (invSlot < 0) return false;
            if (!moveSingleItem(container, invSlot, gridSlot)) return false;
        }
        return true;
    }

    private boolean moveSingleItem(ContainerWorkbench container, int fromSlot, int toSlot) {
        Slot from = container.inventorySlots.get(fromSlot);
        if (from == null || !from.getHasStack()) return false;

        click(container, fromSlot, 0, ClickType.PICKUP);
        click(container, toSlot, 1, ClickType.PICKUP);
        click(container, fromSlot, 0, ClickType.PICKUP);

        if (!mc.player.inventory.getItemStack().isEmpty()) {
            int empty = findFirstEmptyPlayerSlot(container);
            if (empty >= 0) {
                click(container, empty, 0, ClickType.PICKUP);
            } else {
                click(container, fromSlot, 0, ClickType.PICKUP);
            }
        }

        return true;
    }

    private boolean takeCraftResult(ContainerWorkbench container) {
        Slot result = container.inventorySlots.get(RESULT_SLOT);
        if (result == null || !result.getHasStack()) return false;

        click(container, RESULT_SLOT, 0, ClickType.QUICK_MOVE);
        return true;
    }

    private boolean clearCursorStack(ContainerWorkbench container) {
        ItemStack held = mc.player.inventory.getItemStack();
        if (held == null || held.isEmpty()) return true;

        int empty = findFirstEmptyPlayerSlot(container);
        if (empty >= 0) {
            click(container, empty, 0, ClickType.PICKUP);
            return mc.player.inventory.getItemStack().isEmpty();
        }

        click(container, RESULT_SLOT, 0, ClickType.PICKUP);
        return mc.player.inventory.getItemStack().isEmpty();
    }

    private int findFirstEmptyPlayerSlot(ContainerWorkbench container) {
        for (int i = INV_START; i < container.inventorySlots.size(); i++) {
            Slot slot = container.inventorySlots.get(i);
            if (slot != null && !slot.getHasStack()) return i;
        }
        return -1;
    }

    private int findMatchingInventorySlot(ContainerWorkbench container, ItemStack needed) {
        for (int i = INV_START; i < container.inventorySlots.size(); i++) {
            Slot slot = container.inventorySlots.get(i);
            if (slot == null || !slot.getHasStack()) continue;
            ItemStack stack = slot.getStack();
            if (matchesIngredient(stack, needed)) return i;
        }
        return -1;
    }

    private boolean matchesIngredient(ItemStack stack, ItemStack needed) {
        if (stack.getItem() != needed.getItem()) return false;
        if (stack.getMetadata() != needed.getMetadata()) return false;
        if (stack.hasTagCompound() || needed.hasTagCompound()) {
            return ItemStack.areItemStackTagsEqual(stack, needed);
        }
        return true;
    }

    private int calculateMaxCrafts(ContainerWorkbench container) {
        Map<String, Integer> need = new HashMap<String, Integer>();
        for (int i = 0; i < lastRecipe.length; i++) {
            ItemStack stack = lastRecipe[i];
            if (stack.isEmpty()) continue;
            String key = itemKey(stack);
            Integer current = need.get(key);
            need.put(key, current == null ? 1 : current + 1);
        }
        if (need.isEmpty()) return 0;

        Map<String, Integer> have = new HashMap<String, Integer>();
        for (int i = INV_START; i < container.inventorySlots.size(); i++) {
            Slot slot = container.inventorySlots.get(i);
            if (slot == null || !slot.getHasStack()) continue;
            ItemStack stack = slot.getStack();
            String key = itemKey(stack);
            if (!need.containsKey(key)) continue;
            Integer current = have.get(key);
            have.put(key, (current == null ? 0 : current) + stack.getCount());
        }

        int max = Integer.MAX_VALUE;
        for (Map.Entry<String, Integer> e : need.entrySet()) {
            int available = have.containsKey(e.getKey()) ? have.get(e.getKey()) : 0;
            int possible = available / e.getValue();
            if (possible < max) max = possible;
        }
        if (max == Integer.MAX_VALUE) return 0;
        return Math.max(0, max);
    }

    private String itemKey(ItemStack stack) {
        String reg = String.valueOf(stack.getItem().getRegistryName());
        String tag = stack.hasTagCompound() ? stack.getTagCompound().toString() : "";
        return reg + ":" + stack.getMetadata() + ":" + tag;
    }

    private boolean hasRecipe() {
        for (int i = 0; i < lastRecipe.length; i++) {
            if (!lastRecipe[i].isEmpty()) return true;
        }
        return false;
    }

    private boolean isContainerStillValid(ContainerWorkbench baseline) {
        return mc.player != null
            && mc.player.openContainer == baseline
            && mc.currentScreen instanceof GuiCrafting;
    }

    private void click(ContainerWorkbench container, int slotId, int mouseButton, ClickType type) {
        mc.playerController.windowClick(container.windowId, slotId, mouseButton, type, mc.player);
    }
}
