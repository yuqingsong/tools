package com.lotusyu.net.tcp.netty.server;

import com.lotusyu.net.tcp.netty.NettyTcpBase;
import com.lotusyu.net.tcp.netty.handlers.ChildChannelHandler;
import com.lotusyu.net.tcp.netty.handlers.InputPrinthandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

import static com.lotusyu.net.tcp.netty.handlers.ChildChannelHandler.newLengthFieldBasedFrameDecoder;

/**
 * netty tcp server 一个对象表示一个server
 * @author: yuqingsong
 * @create: 2018-07-19 16:50
 **/
public class NettyTcpServer extends NettyTcpBase {

    private static final Logger LOG = LoggerFactory.getLogger(NettyTcpServer.class);
    private final int port;

    public NettyTcpServer(int port){
        this.port = port;
    }

    public void start() {
        ChannelFuture channelFuture = startInternal();
        try {
            channelFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void startAndBlockExit(){
        ChannelFuture channelFuture = startInternal();
        try {
            ChannelFuture f = channelFuture.sync();
            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }



    public ChannelFuture startInternal() {
        // 配置服务端的NIO线程组
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(getChildHandler());
        // 绑定端口，同步等待成功
        ChannelFuture channelFuture = b.bind(port);
        LOG.info("server starting ,bind on {}",port);
        return channelFuture;
    }


    public void stop(){
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

    }


    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        int port = 1234;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 采用默认值
            }
        }
//        NettyTcpServer nettyTcpServer = new NettyTcpServer(port);
//        nettyTcpServer.setChildHandlerProvider(()->new ChildChannelHandler().addIntLengthFiled().add(new InputPrinthandler((ctx, msg)->{
//            ByteBuf m = (ByteBuf) msg;
//            CompositeByteBuf byteBufs = Unpooled.compositeBuffer().writeInt(m.readableBytes()).addComponent(true,m);
////            String hello = "abcd";
////            byte[] bytes = hello.getBytes();
////
////            ByteBuf byteBuf = Unpooled.buffer().writeInt(bytes.length).writeBytes(bytes);
////            ByteBufUtil.getBytes(byteBufs);
//            ctx.writeAndFlush(byteBufs);
//        })));
//        nettyTcpServer.startAndBlockExit();


            NettyTcpServer nettyTcpServer = new NettyTcpServer(port);
            nettyTcpServer.setChildHandler(new ChildChannelHandler((c)->{
                c.pipeline().addLast(newLengthFieldBasedFrameDecoder()).addLast(
                        new InputPrinthandler((ctx, msg) -> {
                            ByteBuf m = (ByteBuf) msg;
                            CompositeByteBuf byteBufs = Unpooled.compositeBuffer().writeInt(m.readableBytes()).addComponent(true, m);
                            ctx.writeAndFlush(byteBufs);
                        })
                );

            }));
            nettyTcpServer.startAndBlockExit();




    }
}
