package com.x4yi.x4tweaker.module.visuals;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ChestESP extends ESPBase {
    private final List<Entity> cachedEntities = new ArrayList<>();

    public ChestESP() {
        super("ChestESP", "Dibuja cajas alrededor de cofres");
    }

    @Override
    protected List<Entity> getEntities() {
        cachedEntities.clear();
        if (mc.world == null) return cachedEntities;
        List<Entity> loaded = mc.world.loadedEntityList;
        for (int i = 0, size = loaded.size(); i < size; i++) {
            Entity e = loaded.get(i);
            if (e instanceof EntityMinecartChest) cachedEntities.add(e);
        }
        return cachedEntities;
    }


    @Override
    public void onRender3D() {
        if (mc.player == null || mc.world == null) return;

        fillColor(cachedColor);
        String currentStyle = style.getValue();
        boolean drawLines = currentStyle.equals("Lines") || currentStyle.equals("Lines&Boxes");
        boolean drawBoxes = currentStyle.equals("Boxes") || currentStyle.equals("Lines&Boxes");

        if (drawLines) {
            updateTracerStart(mc.getRenderPartialTicks());
        }


        List<Entity> entities = getEntities();
        List<TileEntity> tileEntities = mc.world.loadedTileEntityList;
        boolean hasEntities = !entities.isEmpty();
        boolean hasTiles = !tileEntities.isEmpty();
        if (!hasEntities && !hasTiles) return;

        pushRenderState();


        for (int i = 0, size = entities.size(); i < size; i++) {
            Entity entity = entities.get(i);
            if (entity == mc.player) continue;
            renderEntity(entity, cachedColor, drawLines, drawBoxes);
        }


        for (int i = 0, size = tileEntities.size(); i < size; i++) {
            TileEntity te = tileEntities.get(i);
            if (!(te instanceof TileEntityChest)) continue;

            BlockPos pos = te.getPos();
            double x = pos.getX() - mc.getRenderManager().viewerPosX;
            double y = pos.getY() - mc.getRenderManager().viewerPosY;
            double z = pos.getZ() - mc.getRenderManager().viewerPosZ;

            AxisAlignedBB bb = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
            renderBounds(bb, cachedColor, drawLines, drawBoxes);
        }

        popRenderState();
    }

    @Override
    protected void fillColor(float[] out) {
        out[0] = 1.0f; out[1] = 0.64f; out[2] = 0.0f; out[3] = 1.0f;
    }
}
