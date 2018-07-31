package com.lotusyu.net.tcp.netty.handlers;

import io.netty.channel.ChannelHandlerContext;

import java.util.function.BiConsumer;

/**
 * 用于处理接收到的消息,使用setAutoRead限制读取速度，以防止读取速度过快导致内存溢出。
 * @author: yuqingsong
 * @create: 2018-07-30 16:50
 **/
public class RateLimitingHandler extends InputHandler {


    public RateLimitingHandler(BiConsumer<ChannelHandlerContext, Object> onMessage) {
        super(onMessage);
    }

    public RateLimitingHandler() {
        super();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
        //使用setAutoRead限制读取速度，以防止读取速度过快导致内存溢出。
        ctx.channel().config().setAutoRead(ctx.channel().isWritable());
    }


}
