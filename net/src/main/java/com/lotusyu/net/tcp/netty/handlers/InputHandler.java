package com.lotusyu.net.tcp.netty.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.function.BiConsumer;

/**
 * 用于处理接收到的消息
 *
 * @author: yuqingsong
 * @create: 2018-07-19 16:50
 **/
public class InputHandler extends ChannelInboundHandlerAdapter {

    private BiConsumer<ChannelHandlerContext, Object> onMessage;

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

}
