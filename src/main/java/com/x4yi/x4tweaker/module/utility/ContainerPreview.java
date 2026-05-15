package com.x4yi.x4tweaker.module.utility;

import com.x4yi.x4tweaker.event.Event;
import com.x4yi.x4tweaker.event.KeyEvent;
import com.x4yi.x4tweaker.module.Category;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.setting.BooleanSetting;
import com.x4yi.x4tweaker.setting.ModeSetting;
import com.x4yi.x4tweaker.setting.NumberSetting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.input.Keyboard;

public class ContainerPreview extends Module {
    private final ModeSetting position = new ModeSetting("Position", "Posicion del preview", "RIGHT_CROSSHAIR", "LEFT_CROSSHAIR", "RIGHT_CROSSHAIR", "BOTTOM_RIGHT", "CUSTOM");
    private final NumberSetting scale = new NumberSetting("Scale", "Escala", 1.0D, 0.4D, 1.8D, 0.05D);
    private final NumberSetting opacity = new NumberSetting("Opacity", "Opacidad", 0.92D, 0.2D, 1.0D, 0.02D);
    private final BooleanSetting fadeAnimation = new BooleanSetting("Fade Animation", "Animacion fade", true);
    private final BooleanSetting blurBackground = new BooleanSetting("Blur Background", "Fondo suavizado", false);

    private final NumberSetting showDelayMs = new NumberSetting("Show Delay", "Delay de activacion en ms", 220D, 0D, 1200D, 20D);
    private final ModeSetting activationMode = new ModeSetting("Activation", "Activacion", "AUTO", "AUTO", "SHIFT_HOLD", "KEY_HOLD");
    private final NumberSetting activationKey = new NumberSetting("Activation Key", "Tecla de activacion", Keyboard.KEY_C, 1D, 255D, 1D);

    private final NumberSetting refreshMs = new NumberSetting("Refresh Rate", "Refresco de cache ms", 120D, 40D, 1000D, 10D);
    private final NumberSetting customX = new NumberSetting("Custom X", "Posicion X", 0D, -500D, 500D, 5D);
    private final NumberSetting customY = new NumberSetting("Custom Y", "Posicion Y", 0D, -300D, 300D, 5D);

    private final PreviewState state = new PreviewState();
    private final SnapshotCollector collector = new SnapshotCollector();
    private final SnapshotRenderer renderer = new SnapshotRenderer();

    private boolean keyHoldPressed;

    public ContainerPreview() {
        super("ContainerPreview", "Preview visual de contenedores", Category.UTILITY);
        activationKey.withVisibilityCondition(() -> "KEY_HOLD".equals(activationMode.getValue()));
        customX.withVisibilityCondition(() -> "CUSTOM".equals(position.getValue()));
        customY.withVisibilityCondition(() -> "CUSTOM".equals(position.getValue()));
        addSetting(position);
        addSetting(scale);
        addSetting(opacity);
        addSetting(fadeAnimation);
        addSetting(blurBackground);
        addSetting(showDelayMs);
        addSetting(activationMode);
        addSetting(activationKey);
        addSetting(refreshMs);
        addSetting(customX);
        addSetting(customY);
    }

    @Override
    public void onDisable() {
        state.clearAll();
        keyHoldPressed = false;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) {
            state.clearAll();
            return;
        }

        if (!isActivationActive()) {
            state.visible = false;
            return;
        }

        RayTraceResult rt = mc.objectMouseOver;
        if (rt == null || rt.typeOfHit != RayTraceResult.Type.BLOCK || rt.getBlockPos() == null) {
            state.clearTarget();
            return;
        }

        TileEntity te = mc.world.getTileEntity(rt.getBlockPos());
        if (te == null || te.isInvalid()) {
            state.clearTarget();
            return;
        }

        BlockPos pos = rt.getBlockPos();
        long now = System.currentTimeMillis();

        if (!pos.equals(state.targetPos)) {
            state.targetPos = pos.toImmutable();
            state.firstSeenAt = now;
            state.lastRefreshAt = 0L;
            state.fadeStartAt = 0L;
            state.visible = false;
        }

        if (now - state.firstSeenAt < showDelayMs.getValue().longValue()) {
            return;
        }

        if (now - state.lastRefreshAt >= refreshMs.getValue().longValue()) {
            collector.fillSnapshot(te, state);
            state.lastRefreshAt = now;
        }

        state.visible = state.valid;
        if (state.visible && state.fadeStartAt == 0L) {
            state.fadeStartAt = now;
        }
    }

    @Override
    public void onRender2D() {
        if (!state.visible || !state.valid || mc.currentScreen != null) return;
        renderer.render(state);
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof KeyEvent)) return;
        if (!"KEY_HOLD".equals(activationMode.getValue())) return;
        int key = ((KeyEvent) event).getKey();
        if (key == activationKey.getValue().intValue()) {
            keyHoldPressed = Keyboard.isKeyDown(key);
        }
    }

    private boolean isActivationActive() {
        String mode = activationMode.getValue();
        if ("AUTO".equals(mode)) return true;
        if ("SHIFT_HOLD".equals(mode)) return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        if ("KEY_HOLD".equals(mode)) {
            int key = activationKey.getValue().intValue();
            return keyHoldPressed || Keyboard.isKeyDown(key);
        }
        return false;
    }

    private int[] resolvePosition(int sw, int sh, int width, int height, double s) {
        String p = position.getValue();
        int scaledW = (int) (width * s);
        int scaledH = (int) (height * s);
        if ("LEFT_CROSSHAIR".equals(p)) return new int[]{sw / 2 - 10 - scaledW, sh / 2 - scaledH / 2};
        if ("RIGHT_CROSSHAIR".equals(p)) return new int[]{sw / 2 + 10, sh / 2 - scaledH / 2};
        if ("BOTTOM_RIGHT".equals(p)) return new int[]{sw - scaledW - 12, sh - scaledH - 20};
        if ("CUSTOM".equals(p)) return new int[]{(int) (sw / 2 + customX.getValue()), (int) (sh / 2 + customY.getValue())};
        return new int[]{sw / 2 + 10, sh / 2 - scaledH / 2};
    }

    private static int rgba(int r, int g, int b, int a) {
        return (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
    }

    private static class PreviewState {
        private BlockPos targetPos;
        private long firstSeenAt;
        private long lastRefreshAt;
        private long fadeStartAt;
        private boolean visible;
        private boolean valid;
        private int rows;
        private int cols;
        private int slotCount;
        private String title = "Container";
        private ItemStack[] items = new ItemStack[0];

        private void ensure(int size) {
            if (size <= 0) size = 1;
            if (items.length != size) {
                items = new ItemStack[size];
            }
            for (int i = 0; i < size; i++) {
                if (items[i] == null) items[i] = ItemStack.EMPTY;
            }
            slotCount = size;
        }

        private void clearTarget() {
            targetPos = null;
            visible = false;
            valid = false;
            fadeStartAt = 0L;
            rows = 0;
            cols = 0;
            slotCount = 0;
            title = "Container";
        }

        private void clearAll() {
            clearTarget();
            firstSeenAt = 0L;
            lastRefreshAt = 0L;
        }
    }

    private class SnapshotCollector {
        private void fillSnapshot(TileEntity te, PreviewState out) {
            out.valid = false;
            out.rows = 0;
            out.cols = 0;
            out.slotCount = 0;
            out.title = "Container";

            if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                if (handler != null && collectFromHandler(handler, te, out)) {
                    out.valid = true;
                    return;
                }
            }

            if (te instanceof IInventory) {
                IInventory inv = (IInventory) te;
                if (collectFromInventory(inv, out)) {
                    out.valid = true;
                }
            }
        }

        private boolean collectFromHandler(IItemHandler handler, TileEntity te, PreviewState out) {
            int size = handler.getSlots();
            if (size <= 0) return false;

            out.ensure(size);
            for (int i = 0; i < size; i++) {
                ItemStack st = handler.getStackInSlot(i);
                out.items[i] = (st == null || st.isEmpty()) ? ItemStack.EMPTY : st.copy();
            }
            fillLayout(out, size);
            out.title = resolveTileTitle(te);
            return isLayoutValid(out);
        }

        private boolean collectFromInventory(IInventory inv, PreviewState out) {
            int size = inv.getSizeInventory();
            if (size <= 0) return false;

            out.ensure(size);
            for (int i = 0; i < size; i++) {
                ItemStack st = inv.getStackInSlot(i);
                out.items[i] = (st == null || st.isEmpty()) ? ItemStack.EMPTY : st.copy();
            }
            fillLayout(out, size);
            out.title = sanitizeTitle(inv.getDisplayName() != null ? inv.getDisplayName().getFormattedText() : "Container");
            return isLayoutValid(out);
        }

        private boolean isLayoutValid(PreviewState out) {
            return out.cols > 0 && out.rows > 0 && out.slotCount > 0 && out.items.length >= out.slotCount;
        }

        private String resolveTileTitle(TileEntity te) {
            if (te instanceof IInventory) {
                IInventory inv = (IInventory) te;
                return sanitizeTitle(inv.getDisplayName() != null ? inv.getDisplayName().getFormattedText() : "Container");
            }
            return "Container";
        }

        private void fillLayout(PreviewState out, int size) {
            int cols = 9;
            if (size < 9) cols = size;
            else if (size % 9 != 0 && size <= 12) cols = 6;
            int rows = (int) Math.ceil(size / (double) cols);
            out.cols = Math.max(1, cols);
            out.rows = Math.max(1, rows);
        }

        private String sanitizeTitle(String t) {
            return t == null || t.isEmpty() ? "Container" : t;
        }
    }

    private class SnapshotRenderer {
        private void render(PreviewState state) {
            ScaledResolution sr = new ScaledResolution(mc);
            int sw = sr.getScaledWidth();
            int sh = sr.getScaledHeight();

            double s = scale.getValue();
            int width = 14 + state.cols * 18;
            int height = 18 + state.rows * 18;
            int[] pos = resolvePosition(sw, sh, width, height, s);
            int x = pos[0];
            int y = pos[1];

            float alphaMul = opacity.getValue().floatValue();
            if (fadeAnimation.getValue()) {
                long dt = System.currentTimeMillis() - state.fadeStartAt;
                alphaMul *= Math.min(1.0F, dt / 180.0F);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x, (float) y, 0.0F);
            GlStateManager.scale(s, s, 1.0D);

            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if (blurBackground.getValue()) {
                Gui.drawRect(-2, -2, width + 2, height + 2, rgba(0, 0, 0, (int) (140 * alphaMul)));
            }

            Gui.drawRect(0, 0, width, 14, rgba(20, 20, 20, (int) (220 * alphaMul)));
            Gui.drawRect(0, 14, width, height, rgba(35, 35, 35, (int) (160 * alphaMul)));
            mc.fontRenderer.drawStringWithShadow(state.title, 4, 3, rgba(255, 255, 255, (int) (255 * alphaMul)));

            GlStateManager.enableRescaleNormal();
            GlStateManager.enableLighting();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableDepth();

            RenderItem itemRenderer = mc.getRenderItem();
            float prevZ = itemRenderer.zLevel;
            itemRenderer.zLevel = 200.0F;

            int idx = 0;
            for (int r = 0; r < state.rows; r++) {
                int sy = 16 + r * 18;
                for (int c = 0; c < state.cols; c++) {
                    int sx = 7 + c * 18;
                    Gui.drawRect(sx - 1, sy - 1, sx + 17, sy + 17, rgba(10, 10, 10, (int) (110 * alphaMul)));
                    if (idx >= state.slotCount || idx >= state.items.length) {
                        idx++;
                        continue;
                    }

                    ItemStack stack = state.items[idx++];
                    if (stack == null || stack.isEmpty()) continue;

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    itemRenderer.renderItemAndEffectIntoGUI(stack, sx, sy);
                    itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, stack, sx, sy, null);
                }
            }

            itemRenderer.zLevel = prevZ;
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableDepth();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }
}
