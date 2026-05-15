package com.x4yi.x4tweaker.core;

import com.x4yi.x4tweaker.event.PacketEvent;
import com.x4yi.x4tweaker.manager.ModuleEventDispatcher;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.Packet;


public class PacketHandler extends ChannelDuplexHandler {
    private static final String HANDLER_NAME = "x4tweaker_packet_handler";

    private final ModuleEventDispatcher dispatcher;

    public PacketHandler(ModuleEventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Packet) {
            PacketEvent.Receive event = new PacketEvent.Receive((Packet<?>) msg);
            dispatcher.dispatchEventOnly(event);
            if (event.isCancelled()) return;
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Packet) {
            PacketEvent.Send event = new PacketEvent.Send((Packet<?>) msg);
            dispatcher.dispatchEventOnly(event);
            if (event.isCancelled()) return;
        }
        super.write(ctx, msg, promise);
    }

    public static String getHandlerName() {
        return HANDLER_NAME;
    }
}
