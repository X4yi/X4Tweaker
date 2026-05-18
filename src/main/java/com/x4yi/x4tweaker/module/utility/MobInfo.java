package com.x4yi.x4tweaker.module.utility;

import com.x4yi.x4tweaker.module.Category;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.setting.BooleanSetting;
import com.x4yi.x4tweaker.setting.ModeSetting;
import com.x4yi.x4tweaker.setting.NumberSetting;
import com.x4yi.x4tweaker.utils.camera.FakeCameraEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobInfo extends Module {
    private static final int HEARTS_PER_ROW = 10;
    private static final int HEART_WIDTH = 8;
    private static final int HEART_HEIGHT = 9;

    private final ModeSetting renderLocation = new ModeSetting("Render Location", "Posicion del texto", "TOP", "TOP", "SIDE_LEFT", "SIDE_RIGHT", "DIAGONAL", "CUSTOM_OFFSET");
    private final NumberSetting scale = new NumberSetting("Scale", "Escala del texto", 0.02D, 0.01D, 0.06D, 0.005D);
    private final NumberSetting maxDistance = new NumberSetting("Max Distance", "Distancia maxima", 32D, 4D, 96D, 1D);
    private final NumberSetting fadeStart = new NumberSetting("Fade Start", "Inicio del fade por distancia", 20D, 2D, 96D, 1D);
    private final BooleanSetting compactMode = new BooleanSetting("Compact Mode", "Vista compacta", false);
    private final NumberSetting lineSpacing = new NumberSetting("Line Spacing", "Separacion entre lineas", 1D, 0D, 8D, 1D);

    private final ModeSetting visibilityMode = new ModeSetting("Visibility Mode", "Que entidades mostrar", "CROSSHAIR_NEAR", "LOOKED_ONLY", "CROSSHAIR_NEAR", "ALL_NEARBY");
    private final BooleanSetting lineOfSightOnly = new BooleanSetting("Line Of Sight", "Solo con linea de vision", false);
    private final BooleanSetting throughWalls = new BooleanSetting("Render Through Walls", "Renderizar a traves de paredes", false);

    private final NumberSetting crosshairRange = new NumberSetting("Crosshair Range", "Rango alrededor del crosshair", 12D, 2D, 40D, 0.5D);

    private final NumberSetting customOffsetX = new NumberSetting("Offset X", "Offset X custom", 0D, -2D, 2D, 0.05D);
    private final NumberSetting customOffsetY = new NumberSetting("Offset Y", "Offset Y custom", 0.2D, -2D, 2D, 0.05D);
    private final NumberSetting customOffsetZ = new NumberSetting("Offset Z", "Offset Z custom", 0D, -2D, 2D, 0.05D);

    private final Map<String, String> modNameCache = new HashMap<String, String>();
    private final List<EntityLivingBase> renderTargets = new ArrayList<EntityLivingBase>();
    private final double[] sharedOffset = new double[3];
    private final HeartLayout sharedHeartLayout = new HeartLayout();
    private final String[] armorCache = new String[31];

    private String getArmorString(int armor) {
        if (armor >= 0 && armor <= 30) {
            if (armorCache[armor] == null) armorCache[armor] = "[" + armor + " armor]";
            return armorCache[armor];
        }
        return "[" + armor + " armor]";
    }

    public MobInfo() {
        super("MobInfo", "Informacion flotante de entidades", Category.UTILITY);
        crosshairRange.withVisibilityCondition(() -> !"ALL_NEARBY".equals(visibilityMode.getValue()));
        customOffsetX.withVisibilityCondition(() -> "CUSTOM_OFFSET".equals(renderLocation.getValue()));
        customOffsetY.withVisibilityCondition(() -> "CUSTOM_OFFSET".equals(renderLocation.getValue()));
        customOffsetZ.withVisibilityCondition(() -> "CUSTOM_OFFSET".equals(renderLocation.getValue()));
        addSetting(renderLocation);
        addSetting(scale);
        addSetting(maxDistance);
        addSetting(fadeStart);
        addSetting(compactMode);
        addSetting(lineSpacing);
        addSetting(visibilityMode);
        addSetting(lineOfSightOnly);
        addSetting(throughWalls);
        addSetting(crosshairRange);
        addSetting(customOffsetX);
        addSetting(customOffsetY);
        addSetting(customOffsetZ);
    }

    @Override
    public void onRender3D() {
        if (mc.player == null || mc.world == null) return;

        collectTargets();
        if (renderTargets.isEmpty()) return;

        final float partial = mc.getRenderPartialTicks();
        final double maxDistSq = maxDistance.getValue() * maxDistance.getValue();

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        if (throughWalls.getValue()) GlStateManager.disableDepth();
        else GlStateManager.enableDepth();

        Entity viewEntity = mc.getRenderViewEntity();
        if (viewEntity == null) viewEntity = mc.player;

        for (int i = 0, size = renderTargets.size(); i < size; i++) {
            EntityLivingBase entity = renderTargets.get(i);
            if (!isRenderable(entity)) continue;
            double distSq = viewEntity.getDistanceSq(entity);
            if (distSq > maxDistSq) continue;
            
            boolean seen = false;
            if (viewEntity instanceof EntityLivingBase) {
                seen = ((EntityLivingBase) viewEntity).canEntityBeSeen(entity);
            } else {
                seen = mc.player.canEntityBeSeen(entity);
            }
            if (lineOfSightOnly.getValue() && !seen) continue;
            
            renderEntityInfo(entity, partial, (float) Math.sqrt(distSq));
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private void collectTargets() {
        renderTargets.clear();
        String mode = visibilityMode.getValue();
        if ("LOOKED_ONLY".equals(mode)) {
            EntityLivingBase looked = getLookedLiving();
            if (looked != null) renderTargets.add(looked);
            return;
        }

        final double maxDistSq = maxDistance.getValue() * maxDistance.getValue();
        final double crossRange = crosshairRange.getValue();
        final double cosMaxAngle = Math.cos(Math.toRadians(crossRange));

        Entity viewEntity = mc.getRenderViewEntity();
        if (viewEntity == null) viewEntity = mc.player;

        final float partial = mc.getRenderPartialTicks();
        double eyeX = lerp(viewEntity.lastTickPosX, viewEntity.posX, partial);
        double eyeY = lerp(viewEntity.lastTickPosY, viewEntity.posY, partial) + viewEntity.getEyeHeight();
        double eyeZ = lerp(viewEntity.lastTickPosZ, viewEntity.posZ, partial);

        float yaw = viewEntity.prevRotationYaw + (viewEntity.rotationYaw - viewEntity.prevRotationYaw) * partial;
        float pitch = viewEntity.prevRotationPitch + (viewEntity.rotationPitch - viewEntity.prevRotationPitch) * partial;
        float f = net.minecraft.util.math.MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = net.minecraft.util.math.MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -net.minecraft.util.math.MathHelper.cos(-pitch * 0.017453292F);
        float f3 = net.minecraft.util.math.MathHelper.sin(-pitch * 0.017453292F);
        double lookX = f1 * f2;
        double lookY = f3;
        double lookZ = f * f2;

        List<Entity> loaded = mc.world.loadedEntityList;
        for (int i = 0, size = loaded.size(); i < size; i++) {
            Entity e = loaded.get(i);
            if (!(e instanceof EntityLivingBase)) continue;
            EntityLivingBase living = (EntityLivingBase) e;
            if (!isRenderable(living)) continue;

            double distSq = viewEntity.getDistanceSq(living);
            if (distSq > maxDistSq) continue;

            if ("CROSSHAIR_NEAR".equals(mode)) {
                double dot = dotToEntity(eyeX, eyeY, eyeZ, lookX, lookY, lookZ, living, partial);
                if (dot < cosMaxAngle) continue;
            }

            renderTargets.add(living);
        }
    }

    private static double dotToEntity(double eyeX, double eyeY, double eyeZ, double lookX, double lookY, double lookZ, EntityLivingBase entity, float partialTicks) {
        double ex = lerp(entity.lastTickPosX, entity.posX, partialTicks);
        double ey = lerp(entity.lastTickPosY, entity.posY, partialTicks) + entity.height * 0.5D;
        double ez = lerp(entity.lastTickPosZ, entity.posZ, partialTicks);
        double dx = ex - eyeX;
        double dy = ey - eyeY;
        double dz = ez - eyeZ;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len > 0) { dx /= len; dy /= len; dz /= len; }
        return lookX * dx + lookY * dy + lookZ * dz;
    }

    private EntityLivingBase getLookedLiving() {
        RayTraceResult rt = mc.objectMouseOver;
        if (rt == null || rt.typeOfHit != RayTraceResult.Type.ENTITY) return null;
        if (!(rt.entityHit instanceof EntityLivingBase)) return null;
        EntityLivingBase living = (EntityLivingBase) rt.entityHit;
        return isRenderable(living) ? living : null;
    }

    private boolean isRenderable(EntityLivingBase entity) {
        return entity != mc.player
            && !(entity instanceof FakeCameraEntity)
            && entity.isEntityAlive()
            && !entity.isDead;
    }

    private void renderEntityInfo(EntityLivingBase entity, float partialTicks, float distance) {
        double x = lerp(entity.lastTickPosX, entity.posX, partialTicks) - mc.getRenderManager().viewerPosX;
        double y = lerp(entity.lastTickPosY, entity.posY, partialTicks) - mc.getRenderManager().viewerPosY;
        double z = lerp(entity.lastTickPosZ, entity.posZ, partialTicks) - mc.getRenderManager().viewerPosZ;

        double[] offset = getOffset(entity);
        x += offset[0];
        y += offset[1];
        z += offset[2];

        float alpha = getFadeAlpha(distance);
        if (alpha <= 0.01F) return;

        FontRenderer fr = mc.fontRenderer;
        String name = entity.getDisplayName().getFormattedText();
        String modName = getModName(entity);
        int armor = entity.getTotalArmorValue();

        HeartLayout hearts = buildHeartLayout(entity.getHealth(), entity.getMaxHealth());
        int baseLines = compactMode.getValue() ? 2 : 2;
        int lines = baseLines + (compactMode.getValue() ? 1 : hearts.rows) + (armor > 0 ? 1 : 0);

        float textLineHeight = fr.FONT_HEIGHT + lineSpacing.getValue().floatValue();
        float heartsLineHeight = HEART_HEIGHT + lineSpacing.getValue().floatValue();
        float totalHeight = compactMode.getValue()
            ? textLineHeight + heartsLineHeight + textLineHeight + (armor > 0 ? textLineHeight : 0)
            : textLineHeight + textLineHeight + hearts.rows * heartsLineHeight + (armor > 0 ? textLineHeight : 0);

        int width = Math.max(fr.getStringWidth(name), fr.getStringWidth(modName));
        int heartsWidth = hearts.heartsInFirstRow * HEART_WIDTH;
        if (heartsWidth > width) width = heartsWidth;
        int aw = 0;
        if (armor > 0) {
            aw = fr.getStringWidth(getArmorString(armor));
            if (aw > width) width = aw;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        float s = scale.getValue().floatValue();
        GlStateManager.scale(-s, -s, s);

        GlStateManager.disableTexture2D();
        drawRect(-width / 2.0F - 2.0F, -totalHeight - 2.0F, width / 2.0F + 2.0F, 2.0F, ((int) (alpha * 120.0F) << 24));
        GlStateManager.enableTexture2D();

        int color = (((int) (alpha * 255.0F)) << 24) | 0xFFFFFF;
        float yDraw = -totalHeight;

        fr.drawStringWithShadow(name, -fr.getStringWidth(name) / 2.0F, yDraw, color);
        yDraw += textLineHeight;

        if (compactMode.getValue()) {
            int drawnHearts = Math.min(HEARTS_PER_ROW, hearts.totalHearts);
            int hx = -(drawnHearts * HEART_WIDTH) / 2;
            drawHeartsRow(hx, (int) yDraw, drawnHearts, Math.min(drawnHearts, hearts.filledHearts), hearts.halfHeartIndexInRow, alpha);
            yDraw += heartsLineHeight;
            fr.drawStringWithShadow(modName, -fr.getStringWidth(modName) / 2.0F, yDraw, color);
        } else {
            fr.drawStringWithShadow(modName, -fr.getStringWidth(modName) / 2.0F, yDraw, color);
            yDraw += textLineHeight;
            drawHeartsRows((int) yDraw, hearts, alpha);
            yDraw += hearts.rows * heartsLineHeight;
        }

        if (armor > 0) {
            String armorLine = getArmorString(armor);
            fr.drawStringWithShadow(armorLine, -fr.getStringWidth(armorLine) / 2.0F, yDraw, color);
        }

        GlStateManager.popMatrix();
    }

    private void drawHeartsRows(int yStart, HeartLayout layout, float alpha) {
        mc.getTextureManager().bindTexture(Gui.ICONS);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);

        int heartsRemaining = layout.totalHearts;
        int fullRemaining = layout.filledHearts;
        int halfIndexGlobal = layout.halfHeartGlobalIndex;
        int y = yStart;
        int globalIndex = 0;

        for (int row = 0; row < layout.rows; row++) {
            int heartsThisRow = Math.min(HEARTS_PER_ROW, heartsRemaining);
            int rowX = -(heartsThisRow * HEART_WIDTH) / 2;
            for (int i = 0; i < heartsThisRow; i++) {
                int x = rowX + i * HEART_WIDTH;
                drawHeartEmpty(x, y);
                if (globalIndex < fullRemaining) {
                    drawHeartFull(x, y);
                } else if (globalIndex == halfIndexGlobal) {
                    drawHeartHalf(x, y);
                }
                globalIndex++;
            }
            heartsRemaining -= heartsThisRow;
            y += HEART_HEIGHT + lineSpacing.getValue().intValue();
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawHeartsRow(int xStart, int y, int total, int full, int halfIndexInRow, float alpha) {
        mc.getTextureManager().bindTexture(Gui.ICONS);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        for (int i = 0; i < total; i++) {
            int x = xStart + i * HEART_WIDTH;
            drawHeartEmpty(x, y);
            if (i < full) {
                drawHeartFull(x, y);
            } else if (i == halfIndexInRow) {
                drawHeartHalf(x, y);
            }
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawHeartEmpty(int x, int y) {
        drawTexturedRect(x, y, 16, 0, HEART_WIDTH, HEART_HEIGHT);
    }

    private static void drawHeartFull(int x, int y) {
        drawTexturedRect(x, y, 52, 0, HEART_WIDTH, HEART_HEIGHT);
    }

    private static void drawHeartHalf(int x, int y) {
        drawTexturedRect(x, y, 61, 0, HEART_WIDTH, HEART_HEIGHT);
    }

    private static void drawTexturedRect(int x, int y, int u, int v, int w, int h) {
        float inv = 1.0F / 256.0F;
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(u * inv, (v + h) * inv);
        GL11.glVertex3f(x, y + h, 0.0F);
        GL11.glTexCoord2f((u + w) * inv, (v + h) * inv);
        GL11.glVertex3f(x + w, y + h, 0.0F);
        GL11.glTexCoord2f((u + w) * inv, v * inv);
        GL11.glVertex3f(x + w, y, 0.0F);
        GL11.glTexCoord2f(u * inv, v * inv);
        GL11.glVertex3f(x, y, 0.0F);
        GL11.glEnd();
    }

    private HeartLayout buildHeartLayout(float health, float maxHealth) {
        int totalHearts = Math.max(1, (int) Math.ceil(maxHealth / 2.0F));
        int fullHearts = (int) Math.floor(health / 2.0F);
        boolean hasHalf = ((int) Math.ceil(health)) % 2 != 0;
        int rows = (int) Math.ceil(totalHearts / (double) HEARTS_PER_ROW);
        int heartsInFirstRow = Math.min(HEARTS_PER_ROW, totalHearts);
        int halfGlobal = hasHalf ? fullHearts : -1;
        int halfRow = hasHalf && fullHearts < HEARTS_PER_ROW ? fullHearts : -1;
        sharedHeartLayout.update(totalHearts, fullHearts, halfGlobal, halfRow, rows, heartsInFirstRow);
        return sharedHeartLayout;
    }

    private float getFadeAlpha(float distance) {
        float max = maxDistance.getValue().floatValue();
        float start = Math.min(fadeStart.getValue().floatValue(), max);
        if (distance <= start) return 1.0F;
        float t = (distance - start) / (max - start + 0.0001F);
        if (t >= 1.0F) return 0.0F;
        return 1.0F - t;
    }

    private String getModName(EntityLivingBase entity) {
        ResourceLocation key = EntityList.getKey(entity);
        if (key == null) return "Unknown";
        String namespace = key.getResourceDomain();

        String cached = modNameCache.get(namespace);
        if (cached != null) return cached;

        ModContainer container = Loader.instance().getIndexedModList().get(namespace);
        String resolved = container != null ? container.getName() : capitalize(namespace);
        modNameCache.put(namespace, resolved);
        return resolved;
    }

    private static String capitalize(String value) {
        if (value == null || value.isEmpty()) return "Unknown";
        if (value.length() == 1) return value.toUpperCase();
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private static double lerp(double from, double to, float partial) {
        return from + (to - from) * partial;
    }

    private double[] getOffset(EntityLivingBase entity) {
        String location = renderLocation.getValue();
        if ("CUSTOM_OFFSET".equals(location)) {
            sharedOffset[0] = customOffsetX.getValue(); sharedOffset[1] = customOffsetY.getValue(); sharedOffset[2] = customOffsetZ.getValue();
            return sharedOffset;
        }

        double w = entity.width * 0.7D;
        double h = Math.max(0.1D, entity.height);
        if ("TOP".equals(location)) { sharedOffset[0] = 0D; sharedOffset[1] = h + 0.35D; sharedOffset[2] = 0D; return sharedOffset; }
        if ("SIDE_LEFT".equals(location)) { sharedOffset[0] = -w; sharedOffset[1] = h * 0.7D; sharedOffset[2] = 0D; return sharedOffset; }
        if ("SIDE_RIGHT".equals(location)) { sharedOffset[0] = w; sharedOffset[1] = h * 0.7D; sharedOffset[2] = 0D; return sharedOffset; }
        if ("DIAGONAL".equals(location)) { sharedOffset[0] = w * 0.7D; sharedOffset[1] = h * 0.8D; sharedOffset[2] = w * 0.7D; return sharedOffset; }
        sharedOffset[0] = 0D; sharedOffset[1] = h + 0.35D; sharedOffset[2] = 0D; return sharedOffset;
    }

    private static void drawRect(float left, float top, float right, float bottom, int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        GlStateManager.color(r, g, b, a);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(left, bottom, 0.0F);
        GL11.glVertex3f(right, bottom, 0.0F);
        GL11.glVertex3f(right, top, 0.0F);
        GL11.glVertex3f(left, top, 0.0F);
        GL11.glEnd();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static class HeartLayout {
        private int totalHearts;
        private int filledHearts;
        private int halfHeartGlobalIndex;
        private int halfHeartIndexInRow;
        private int rows;
        private int heartsInFirstRow;

        private void update(int totalHearts, int filledHearts, int halfHeartGlobalIndex, int halfHeartIndexInRow, int rows, int heartsInFirstRow) {
            this.totalHearts = totalHearts;
            this.filledHearts = filledHearts;
            this.halfHeartGlobalIndex = halfHeartGlobalIndex;
            this.halfHeartIndexInRow = halfHeartIndexInRow;
            this.rows = rows;
            this.heartsInFirstRow = heartsInFirstRow;
        }
    }
}
