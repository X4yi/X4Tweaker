package com.x4yi.x4tweaker.proxy;

import com.x4yi.x4tweaker.core.X4TweakerClient;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        X4TweakerClient.getInstance().start();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        X4TweakerClient.getInstance().init();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }
}
