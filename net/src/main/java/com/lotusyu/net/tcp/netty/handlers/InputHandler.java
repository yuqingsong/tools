package com.lotusyu.net.tcp.netty.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 用于处理接收到的消息
 *
 * @author: yuqingsong
 * @create: 2018-07-19 16:50
 **/
public class InputHandler extends ChannelInboundHandlerAdapter {

    private BiConsumer<ChannelHandlerContext, Object> onMessage;


    private Consumer<ChannelHandlerContext> onChannelInactive;
    private BiConsumer<ChannelHandlerContext, Object> onUserEventTriggered;


    public InputHandler(BiConsumer<ChannelHandlerContext, Object> onMessage) {
        this.onMessage = onMessage;
    }

    public InputHandler() {
        this.onMessage = (ctx, msg) -> {
            ByteBuf m = (ByteBuf) msg;
            System.out.println(new String(ByteBufUtil.getBytes(m)));
        };
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        this.onMessage.accept(ctx, msg);

    }



    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if(onChannelInactive !=null){
            onChannelInactive.accept(ctx);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if(this.onUserEventTriggered!=null){
            this.onUserEventTriggered.accept(ctx,evt);
        }
    }


    public InputHandler setOnChannelInactive(Consumer<ChannelHandlerContext> onChannelInactive) {
        this.onChannelInactive = onChannelInactive;
        return this;
    }

    public BiConsumer<ChannelHandlerContext, Object> getOnUserEventTriggered() {
        return onUserEventTriggered;
    }

    public InputHandler setOnUserEventTriggered(BiConsumer<ChannelHandlerContext, Object> onUserEventTriggered) {
        this.onUserEventTriggered = onUserEventTriggered;
        return this;
    }
}
