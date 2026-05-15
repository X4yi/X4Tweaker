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
    public ChestESP() {
        super("ChestESP", "Dibuja cajas alrededor de cofres");
    }

    @Override
    protected List<Entity> getEntities() {
        List<Entity> chests = new ArrayList<>();
        if (mc.world == null) return chests;
        List<Entity> loaded = mc.world.loadedEntityList;
        for (int i = 0, size = loaded.size(); i < size; i++) {
            Entity e = loaded.get(i);
            if (e instanceof EntityMinecartChest) chests.add(e);
        }
        return chests;
    }

    @Override
    public void onRender3D() {
        if (mc.player == null || mc.world == null) return;

        super.onRender3D();

        float[] color = getColor();
        String currentStyle = style.getValue();
        boolean drawLines = currentStyle.equals("Lines") || currentStyle.equals("Lines&Boxes");
        boolean drawBoxes = currentStyle.equals("Boxes") || currentStyle.equals("Lines&Boxes");

        List<TileEntity> tileEntities = mc.world.loadedTileEntityList;
        if (tileEntities.isEmpty()) return;

        pushRenderState();

        for (int i = 0, size = tileEntities.size(); i < size; i++) {
            TileEntity te = tileEntities.get(i);
            if (!(te instanceof TileEntityChest)) continue;

            BlockPos pos = te.getPos();
            double x = pos.getX() - mc.getRenderManager().viewerPosX;
            double y = pos.getY() - mc.getRenderManager().viewerPosY;
            double z = pos.getZ() - mc.getRenderManager().viewerPosZ;

            AxisAlignedBB bb = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
            renderBounds(bb, color, drawLines, drawBoxes);
        }

        popRenderState();
    }

    @Override
    protected float[] getColor() {
        return new float[]{1.0f, 0.64f, 0.0f, 1.0f};
    }
}
