package com.x4yi.x4tweaker;

import com.x4yi.x4tweaker.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = X4Tweaker.MODID, name = X4Tweaker.NAME, version = X4Tweaker.VERSION)
public class X4Tweaker {
    public static final String MODID = "x4tweaker";
    public static final String NAME = "X4Tweaker";
    public static final String VERSION = "r1.0.3b1";

    @Mod.Instance
    public static X4Tweaker instance;

    @SidedProxy(clientSide = "com.x4yi.x4tweaker.proxy.ClientProxy", serverSide = "com.x4yi.x4tweaker.proxy.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
