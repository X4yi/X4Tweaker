package com.x4yi.x4tweaker.module.utility;

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
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ContainerPreview extends Module {
    private static final String DEFAULT_TITLE = "Container";

    private final ModeSetting position = new ModeSetting("Position", "Posicion del preview", "RIGHT_CROSSHAIR", "LEFT_CROSSHAIR", "RIGHT_CROSSHAIR", "BOTTOM_RIGHT", "CUSTOM");
    private final NumberSetting scale = new NumberSetting("Scale", "Escala", 1.0D, 0.4D, 1.8D, 0.05D);
    private final NumberSetting opacity = new NumberSetting("Opacity", "Opacidad", 0.92D, 0.2D, 1.0D, 0.02D);
    private final BooleanSetting fadeAnimation = new BooleanSetting("Fade Animation", "Animacion fade", true);
    private final BooleanSetting blurBackground = new BooleanSetting("Blur Background", "Fondo suavizado", false);
    private final NumberSetting showDelayTicks = new NumberSetting("Show Delay", "Delay de activacion en ticks", 4D, 0D, 40D, 1D);
    private final NumberSetting refreshTicks = new NumberSetting("Refresh Rate", "Refresco de cache en ticks", 3D, 1D, 40D, 1D);
    private final NumberSetting customX = new NumberSetting("Custom X", "Posicion X", 0D, -500D, 500D, 5D);
    private final NumberSetting customY = new NumberSetting("Custom Y", "Posicion Y", 0D, -300D, 300D, 5D);

    private final PreviewState state = new PreviewState();
    private final SnapshotCollector collector = new SnapshotCollector();
    private long worldTick;
    private ScaledResolution cachedRes;
    private int lastW, lastH, lastScale;
    private final int[] resolvedPos = new int[2];

    private ScaledResolution getRes() {
        int w = mc.displayWidth;
        int h = mc.displayHeight;
        int s = mc.gameSettings.guiScale;
        if (cachedRes == null || w != lastW || h != lastH || s != lastScale) {
            cachedRes = new ScaledResolution(mc);
            lastW = w; lastH = h; lastScale = s;
        }
        return cachedRes;
    }

    public ContainerPreview() {
        super("ContainerPreview", "Preview visual de contenedores", Category.UTILITY);
        customX.withVisibilityCondition(() -> "CUSTOM".equals(position.getValue()));
        customY.withVisibilityCondition(() -> "CUSTOM".equals(position.getValue()));

        addSetting(position);
        addSetting(scale);
        addSetting(opacity);
        addSetting(fadeAnimation);
        addSetting(blurBackground);
        addSetting(showDelayTicks);
        addSetting(refreshTicks);
        addSetting(customX);
        addSetting(customY);
    }

    @Override
    public void onDisable() {
        state.clear();
    }

    @Override
    public void onUpdate() {
        worldTick++;

        if (mc.player == null || mc.world == null) {
            state.clear();
            return;
        }

        RayTraceResult rt = mc.objectMouseOver;
        if (rt == null || rt.typeOfHit != RayTraceResult.Type.BLOCK || rt.getBlockPos() == null) {
            state.clearTargetOnly();
            return;
        }

        BlockPos pos = rt.getBlockPos();
        TileEntity te = mc.world.getTileEntity(pos);
        if (te == null || te.isInvalid()) {
            state.clearTargetOnly();
            return;
        }

        if (state.targetPos == null || !state.targetPos.equals(pos)) {
            state.bindTarget(pos, worldTick);
        }

        if ((worldTick - state.firstSeenTick) < showDelayTicks.getValue().intValue()) {
            state.visible = false;
            return;
        }

        int refresh = refreshTicks.getValue().intValue();
        if (!state.valid || (worldTick - state.lastRefreshTick) >= refresh) {
            collector.collect(te, state);
            state.lastRefreshTick = worldTick;
        }

        state.visible = state.valid;
        if (state.visible && state.fadeStartTick < 0L) {
            state.fadeStartTick = worldTick;
        }
    }

    @Override
    public void onRender2D() {
        if (!state.visible || !state.valid || mc.currentScreen != null) return;
        renderPreview();
    }

    private void renderPreview() {
        ScaledResolution sr = getRes();
        int sw = sr.getScaledWidth();
        int sh = sr.getScaledHeight();

        int baseWidth = 14 + state.cols * 18;
        int baseHeight = 18 + state.rows * 18;
        double scaleValue = scale.getValue();

        int scaledWidth = (int) Math.ceil(baseWidth * scaleValue);
        int scaledHeight = (int) Math.ceil(baseHeight * scaleValue);
        int[] pos = resolvePosition(sw, sh, scaledWidth, scaledHeight);

        float alphaMul = opacity.getValue().floatValue();
        if (fadeAnimation.getValue()) {
            long dt = worldTick - state.fadeStartTick;
            float f = dt <= 0 ? 0.0F : Math.min(1.0F, dt / 8.0F);
            alphaMul *= f;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(pos[0], pos[1], 0.0F);
        GlStateManager.scale(scaleValue, scaleValue, 1.0D);

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();

        if (blurBackground.getValue()) {
            Gui.drawRect(-2, -2, baseWidth + 2, baseHeight + 2, rgba(0, 0, 0, (int) (140 * alphaMul)));
        }

        Gui.drawRect(0, 0, baseWidth, 14, rgba(20, 20, 20, (int) (220 * alphaMul)));
        Gui.drawRect(0, 14, baseWidth, baseHeight, rgba(35, 35, 35, (int) (170 * alphaMul)));
        mc.fontRenderer.drawStringWithShadow(state.title, 4, 3, rgba(255, 255, 255, (int) (255 * alphaMul)));

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableLighting();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();

        RenderItem itemRenderer = mc.getRenderItem();
        float oldZ = itemRenderer.zLevel;
        itemRenderer.zLevel = 200.0F;

        int slotIdx = 0;
        for (int row = 0; row < state.rows; row++) {
            int y = 16 + row * 18;
            for (int col = 0; col < state.cols; col++) {
                int x = 7 + col * 18;
                Gui.drawRect(x - 1, y - 1, x + 17, y + 17, rgba(10, 10, 10, (int) (110 * alphaMul)));

                if (slotIdx >= state.slotCount || slotIdx >= state.items.length) {
                    slotIdx++;
                    continue;
                }

                ItemStack stack = state.items[slotIdx++];
                if (stack == null || stack.isEmpty()) continue;

                itemRenderer.renderItemAndEffectIntoGUI(stack, x, y);
                itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, stack, x, y, null);
            }
        }

        itemRenderer.zLevel = oldZ;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableDepth();
        GlStateManager.popMatrix();
    }

    private int[] resolvePosition(int screenW, int screenH, int scaledW, int scaledH) {
        int x;
        int y;

        String mode = position.getValue();
        if ("LEFT_CROSSHAIR".equals(mode)) {
            x = screenW / 2 - 10 - scaledW;
            y = screenH / 2 - scaledH / 2;
        } else if ("RIGHT_CROSSHAIR".equals(mode)) {
            x = screenW / 2 + 10;
            y = screenH / 2 - scaledH / 2;
        } else if ("BOTTOM_RIGHT".equals(mode)) {
            x = screenW - scaledW - 12;
            y = screenH - scaledH - 20;
        } else if ("CUSTOM".equals(mode)) {
            x = (int) (screenW / 2 + customX.getValue());
            y = (int) (screenH / 2 + customY.getValue());
        } else {
            x = screenW / 2 + 10;
            y = screenH / 2 - scaledH / 2;
        }

        if (x < 2) x = 2;
        if (y < 2) y = 2;
        if (x + scaledW > screenW - 2) x = screenW - scaledW - 2;
        if (y + scaledH > screenH - 2) y = screenH - scaledH - 2;

        resolvedPos[0] = x;
        resolvedPos[1] = y;
        return resolvedPos;
    }

    private static int rgba(int r, int g, int b, int a) {
        return (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
    }

    private static final class PreviewState {
        private BlockPos targetPos;
        private long firstSeenTick;
        private long lastRefreshTick;
        private long fadeStartTick = -1L;
        private boolean visible;
        private boolean valid;

        private int rows;
        private int cols;
        private int slotCount;
        private String title = DEFAULT_TITLE;
        private ItemStack[] items = new ItemStack[0];

        private void bindTarget(BlockPos pos, long tick) {
            targetPos = pos.toImmutable();
            firstSeenTick = tick;
            lastRefreshTick = -1L;
            fadeStartTick = -1L;
            valid = false;
            visible = false;
            rows = 0;
            cols = 0;
            slotCount = 0;
            title = DEFAULT_TITLE;
        }

        private void ensureCapacity(int size) {
            if (size <= 0) size = 1;
            if (items.length != size) {
                items = new ItemStack[size];
                for (int i = 0; i < size; i++) {
                    items[i] = ItemStack.EMPTY;
                }
            }
            slotCount = size;
        }

        private void clearTargetOnly() {
            targetPos = null;
            visible = false;
            valid = false;
            fadeStartTick = -1L;
            rows = 0;
            cols = 0;
            slotCount = 0;
            title = DEFAULT_TITLE;
        }

        private void clear() {
            clearTargetOnly();
            firstSeenTick = 0L;
            lastRefreshTick = 0L;
        }
    }

    private static final class SnapshotCollector {
        private void collect(TileEntity te, PreviewState out) {
            out.valid = false;
            out.rows = 0;
            out.cols = 0;
            out.slotCount = 0;
            out.title = resolveTitle(te);

            if (te instanceof IInventory) {
                IInventory inv = (IInventory) te;
                if (collectFromInventory(inv, out)) {
                    out.valid = true;
                    return;
                }
            }

            if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                if (handler != null && collectFromHandler(handler, te, out)) {
                    out.valid = true;
                    return;
                }
            }
        }

        private boolean collectFromHandler(IItemHandler handler, TileEntity te, PreviewState out) {
            int size = handler.getSlots();
            if (size <= 0) return false;

            out.ensureCapacity(size);
            for (int i = 0; i < size; i++) {
                ItemStack stack = handler.getStackInSlot(i);
                out.items[i] = (stack == null || stack.isEmpty()) ? ItemStack.EMPTY : stack.copy();
            }
            fillLayout(out, size);
            out.title = resolveTitle(te);
            return isLayoutValid(out);
        }

        private boolean collectFromInventory(IInventory inv, PreviewState out) {
            int size = inv.getSizeInventory();
            if (size <= 0) return false;

            out.ensureCapacity(size);
            for (int i = 0; i < size; i++) {
                ItemStack stack = inv.getStackInSlot(i);
                out.items[i] = (stack == null || stack.isEmpty()) ? ItemStack.EMPTY : stack.copy();
            }
            fillLayout(out, size);
            out.title = sanitizeTitle(inv.getDisplayName() == null ? null : inv.getDisplayName().getFormattedText());
            return isLayoutValid(out);
        }

        private boolean isLayoutValid(PreviewState state) {
            return state.cols > 0 && state.rows > 0 && state.slotCount > 0 && state.items.length >= state.slotCount;
        }

        private String resolveTitle(TileEntity te) {
            if (te instanceof IInventory) {
                IInventory inv = (IInventory) te;
                String title = inv.getDisplayName() == null ? null : inv.getDisplayName().getFormattedText();
                if (title != null && !title.trim().isEmpty()) return sanitizeTitle(title);
            }
            if (te.getBlockType() != null && te.getBlockType() != Blocks.AIR) {
                try {
                    return sanitizeTitle(I18n.format(te.getBlockType().getUnlocalizedName() + ".name"));
                } catch (Exception ignored) {}
            }
            return DEFAULT_TITLE;
        }

        private String sanitizeTitle(String title) {
            if (title == null || title.isEmpty()) return DEFAULT_TITLE;
            return title;
        }

        private void fillLayout(PreviewState state, int size) {
            int cols;
            if (size <= 5) cols = size;
            else if (size == 27 || size == 54) cols = 9;
            else if (size <= 12 && size % 3 == 0) cols = 3;
            else if (size % 9 == 0) cols = 9;
            else if (size % 6 == 0) cols = 6;
            else cols = Math.min(9, size);

            int rows = (int) Math.ceil(size / (double) cols);
            state.cols = Math.max(1, cols);
            state.rows = Math.max(1, rows);
        }
    }
}
