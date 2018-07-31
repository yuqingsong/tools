package com.lotusyu.net.tcp.netty.server;

import com.lotusyu.net.tcp.netty.NettyTcpBase;
import com.lotusyu.net.tcp.netty.handlers.ChildChannelHandler;
import com.lotusyu.net.tcp.netty.handlers.RateLimitingHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.*;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

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

    public ChannelFuture start() {
        ChannelFuture channelFuture = startInternal();
        try {
            channelFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return channelFuture;
    }

    public void startAndBlockExit(){
        ChannelFuture channelFuture = startInternal();
        waitClose(channelFuture);

    }

    public static void waitClose(ChannelFuture channelFuture) {
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
//                .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
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
//        nettyTcpServer.setChildHandlerProvider(()->new ChildChannelHandler().addIntLengthFiled().add(new InputHandler((ctx, msg)->{
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


        ChannelFuture channelFuture = startServer4Test(port);

        NettyTcpServer.waitClose(channelFuture);


    }

    public static ChannelFuture startServer4Test(int port) {
        NettyTcpServer nettyTcpServer = new NettyTcpServer(port);
        AtomicInteger receiveCount = new AtomicInteger();
        nettyTcpServer.setChildHandler(new ChildChannelHandler((c)->{
            c.config().setWriteBufferHighWaterMark(1024*1024);
            c.pipeline().addLast(newLengthFieldBasedFrameDecoder()).addLast(
                    new RateLimitingHandler((ctx, msg) -> {
                        ByteBuf m = (ByteBuf) msg;
//                            pringByteBufInfo(m);
                        CompositeByteBuf byteBufs = Unpooled.compositeBuffer().addComponents(true, Unpooled.directBuffer(4).writeInt(m.readableBytes()),m);
//                            pringByteBufInfo(byteBufs);
                        int i = receiveCount.incrementAndGet();
                        if(i%1000000==0){
                            System.out.println("receive messages :"+i);
                        }
                        ctx.write(byteBufs);
                    })
            );

        }));
        return nettyTcpServer.start();
    }

    public static void pringByteBufInfo(ByteBuf byteBufs) {
        int len = byteBufs.readableBytes();
        System.out.println(len);
        byteBufs.markReaderIndex();
        byte[] content = new byte[len];
        byteBufs.readBytes(content);
        System.out.println(new String(content));
        byteBufs.resetReaderIndex();
    }
}
