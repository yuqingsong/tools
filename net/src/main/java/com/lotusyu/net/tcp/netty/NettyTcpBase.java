package com.lotusyu.net.tcp.netty;

import com.lotusyu.net.tcp.netty.handlers.ChildChannelHandler;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.function.Supplier;
/**
 * netty tcpclient 和 tcpserver 的父类
 * @author: yuqingsong
 * @create: 2018-07-19 16:50
 **/
public class NettyTcpBase {
    protected NioEventLoopGroup workerGroup;
    protected NioEventLoopGroup bossGroup;



    private ChildChannelHandler childHandler;


    public ChildChannelHandler getChildHandler() {
        return childHandler;
    }

    public void setChildHandler(ChildChannelHandler childHandler) {
        this.childHandler = childHandler;
    }





}
