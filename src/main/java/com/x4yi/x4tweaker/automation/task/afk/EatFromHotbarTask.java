package com.x4yi.x4tweaker.automation.task.afk;

import com.x4yi.x4tweaker.automation.BotTask;
import com.x4yi.x4tweaker.automation.control.InventoryController;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class EatFromHotbarTask extends BotTask {
    private final InventoryController inventoryController;
    private final int targetFoodLevel;

    private enum EatPhase {
        SELECTING,
        EATING
    }

    private EatPhase eatPhase;
    private int selectDelay;

    private int totalTicks;
    private int itemTicks;
    private int currentItemDuration;
    private int retryCount;
    private boolean shouldRestore;

    private static final int MAX_TOTAL_TICKS = 200;
    private static final int MAX_RETRIES = 3;
    private static final int SAFETY_MARGIN = 10;
    private static final int RETRY_GRACE_TICKS = 5;

    public EatFromHotbarTask(InventoryController inventoryController, int targetFoodLevel) {
        this.inventoryController = inventoryController;
        this.targetFoodLevel = Math.max(1, Math.min(20, targetFoodLevel));
    }

    @Override
    public String getName() {
        return "EatFromHotbarTask";
    }

    @Override
    public void onStart() {
        totalTicks = 0;
        itemTicks = 0;
        retryCount = 0;
        currentItemDuration = 0;
        shouldRestore = true;
        startEating();
    }

    private void startEating() {
        EntityPlayerSP player = mc.player;
        if (player == null) { stop(); return; }

        if (!inventoryController.selectFoodFromHotbar()) {
            stop();
            return;
        }

        eatPhase = EatPhase.SELECTING;
        selectDelay = 2;
    }

    @Override
    public void onUpdate() {
        EntityPlayerSP player = mc.player;
        if (player == null) { stop(); return; }

        totalTicks++;
        if (totalTicks > MAX_TOTAL_TICKS) {
            stop();
            return;
        }

        if (player.getFoodStats().getFoodLevel() >= targetFoodLevel) {
            stop();
            return;
        }

        if (eatPhase == EatPhase.SELECTING) {
            selectDelay--;
            if (selectDelay <= 0) {
                ItemStack held = player.getHeldItemMainhand();
                if (held.isEmpty()) { stop(); return; }

                currentItemDuration = held.getMaxItemUseDuration() + SAFETY_MARGIN;
                itemTicks = 0;

                mc.playerController.processRightClick(player, mc.world, EnumHand.MAIN_HAND);
                eatPhase = EatPhase.EATING;
            }
            return;
        }

        if (eatPhase == EatPhase.EATING) {
            itemTicks++;

            if (!player.isHandActive()) {
                if (itemTicks < RETRY_GRACE_TICKS) {
                    mc.playerController.processRightClick(player, mc.world, EnumHand.MAIN_HAND);
                    return;
                }

                if (itemTicks >= currentItemDuration - SAFETY_MARGIN) {
                    if (player.getFoodStats().getFoodLevel() < targetFoodLevel) {
                        retryCount++;
                        if (retryCount > MAX_RETRIES) {
                            stop();
                            return;
                        }
                        shouldRestore = false;
                        startEating();
                        return;
                    }
                }

                stop();
                return;
            }

            if (itemTicks > currentItemDuration) {
                retryCount++;
                if (retryCount > MAX_RETRIES) {
                    stop();
                    return;
                }
                player.stopActiveHand();
                shouldRestore = false;
                startEating();
            }
        }
    }

    @Override
    public void onStop() {
        if (mc.player != null && mc.player.isHandActive()) {
            mc.player.stopActiveHand();
        }
        if (shouldRestore) {
            inventoryController.restoreSlot();
        }
    }
}
