package com.lotusyu.net.tcp.netty.client;

/**
 * @author: yuqingsong
 * @create: 2018-07-19 16:50
 **/

import com.lotusyu.net.tcp.netty.NettyTcpBase;
import com.lotusyu.net.tcp.netty.handlers.ChildChannelHandler;
import com.lotusyu.net.tcp.netty.handlers.InputPrinthandler;
import com.lotusyu.net.tcp.netty.server.NettyTcpServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lotusyu.net.tcp.netty.handlers.ChildChannelHandler.newLengthFieldBasedFrameDecoder;

/**
 * netty 实现的TCPClient 一个对象表示一个连接
 * @author: yuqingsong
 * @create: 2018-07-19 16:50
 **/

public class NettyTcpClient extends NettyTcpBase {

    private static final Logger LOG = LoggerFactory.getLogger(NettyTcpClient.class);
    private String host = System.getProperty("host", "127.0.0.1");
    private int port = Integer.parseInt(System.getProperty("port", "8080"));




    public NettyTcpClient(String host,int port){
        this.host = host;
        this.port = port;
    }

    public NettyTcpClient(int port){
        this("localhost",port);
    }

    public Channel connect(){
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(getChildHandler());

            // Start the client.
            ChannelFuture f = b.connect(host, port);
            return f.sync().channel();
        } catch (Exception e){
            LOG.error("连接异常",e);
        }
//        finally {
//            // Shut down the event loop to terminate all threads.
//            group.shutdownGracefully();
//        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        int port = 1234;
        String host = "localhost";
        int msgNum = 1000000;
        if(args!=null && args.length>1){
            port = Integer.parseInt(args[1]);
            host = args[0];
            if(args.length>2){
                msgNum = Integer.parseInt(args[2]);
            }
        }

        NettyTcpClient client = new NettyTcpClient(host,port);
        CountDownLatch c = new CountDownLatch(1);
        AtomicInteger msgCounter = new AtomicInteger();
        int finalMsgNum = msgNum;
        client.setChildHandler(new ChildChannelHandler((ch)->{
            ch.pipeline().addLast(newLengthFieldBasedFrameDecoder()).addLast(
                    new InputPrinthandler((ctx, msg) -> {
                        ByteBuf m = (ByteBuf) msg;
                        m.release();
                        if(msgCounter.incrementAndGet()==finalMsgNum){
                            System.out.println("completed");
                            c.countDown();
                        }
                    })
            );
        }));
        Channel connect = client.connect();
        String msg = new Random().ints().limit(100).collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
        byte[] bytes = msg.getBytes();

        long start = System.currentTimeMillis();
        for (int i = 0; i < msgNum; i++) {
            ByteBuf msgBuf = Unpooled.buffer(bytes.length+4).writeInt(bytes.length).writeBytes(bytes);
            connect.write(msgBuf);
            if(i%10000==0){
                connect.flush();
                while(!connect.isWritable()){
                    System.out.println("sleep");
                    Thread.sleep(100);
                }
            }
        }
        connect.flush();
        c.await();
        long end = System.currentTimeMillis();
        long cost = end -start;
        System.out.println("cost:"+cost+"\tqps:"+msgNum*1000L/cost);

    }
}
