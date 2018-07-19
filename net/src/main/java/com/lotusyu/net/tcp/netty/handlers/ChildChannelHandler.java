package com.lotusyu.net.tcp.netty.handlers;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * netty 用于初始化channel
 * @author: yuqingsong
 * @create: 2018-07-19 16:50
 **/

public class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

    private static final Logger LOG = LoggerFactory.getLogger(ChannelInitializer.class);

    private List<ChannelHandler> handlers = new ArrayList();

    private Consumer<SocketChannel> onInit;

    public ChildChannelHandler(Consumer<SocketChannel> onInit) {
        this.onInit = onInit;
    }

//    public ChildChannelHandler(){
//        this.onInit = (c,t)->{
//            if (handlers.isEmpty()) {
//                LengthFieldBasedFrameDecoder lengthFieldBasedFrameDecoder = newLengthFieldBasedFrameDecoder();
//                InputPrinthandler inputhandler = new InputPrinthandler();
//                c.pipeline().addLast(lengthFieldBasedFrameDecoder).addLast(inputhandler);
//            } else {
//                handlers.forEach(h -> c.pipeline().addLast(h));
//            }
//        };
//    }


    @Override
    protected void initChannel(SocketChannel c) {
        LOG.debug("init channel,thread is {}", Thread.currentThread());
        this.onInit.accept(c);
//        this.addInternal(c);
    }

//    private void addInternal(SocketChannel c) {
//        if (handlers.isEmpty()) {
//            LengthFieldBasedFrameDecoder lengthFieldBasedFrameDecoder = newLengthFieldBasedFrameDecoder();
//            InputPrinthandler inputhandler = new InputPrinthandler();
//            c.pipeline().addLast(lengthFieldBasedFrameDecoder).addLast(inputhandler);
//        } else {
//            handlers.forEach(h -> c.pipeline().addLast(h));
//        }
//    }


    static public LengthFieldBasedFrameDecoder newLengthFieldBasedFrameDecoder() {
        return new LengthFieldBasedFrameDecoder(10 * 1024 * 1024, 0, 4, 0, 4);
    }


//    public ChildChannelHandler add(ChannelHandler... handlerArray) {
//        for (ChannelHandler handler : handlerArray) {
//            handlers.add(handler);
//        }
//        return this;
//    }
//
//    public ChildChannelHandler addIntLengthFiled() {
//        this.add(newLengthFieldBasedFrameDecoder());
//        return this;
//    }


}
