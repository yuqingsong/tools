package com.lotusyu.net.tcp.netty.client;

/**
 * @author: yuqingsong
 * @create: 2018-07-19 16:50
 **/

import com.lotusyu.net.tcp.netty.NettyTcpBase;
import com.lotusyu.net.tcp.netty.handlers.ChildChannelHandler;
import com.lotusyu.net.tcp.netty.handlers.InputHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

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

    private EventLoopGroup group = new NioEventLoopGroup();
    private volatile Channel channel;


    public NettyTcpClient(String host,int port){
        this.host = host;
        this.port = port;
    }

    public NettyTcpClient(int port){
        this("localhost",port);
    }

    public synchronized Channel connect(){

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(getChildHandler());

            // Start the client.
            ChannelFuture f = b.connect(host, port);
            f.addListener((ChannelFuture future) ->{
                if(!future.isSuccess()){
                    EventLoop loop = future.channel().eventLoop();
                    loop.schedule(()->{reconnect(NettyTcpClient.this);},1,TimeUnit.SECONDS);
                }
            });
            Channel channel = f.sync().channel();
            return this.channel=channel;
        } catch (Exception e){
            LOG.error("连接异常",e);
        }
//        finally {
//            // Shut down the event loop to terminate all threads.
//            group.shutdownGracefully();
//        }
        return null;
    }

    public void send(ByteBuf msg){
        channel.writeAndFlush(msg);

    }

    public static void main(String[] args) throws Exception {
        int port = 1234;
        String host = "localhost";
//        host = "192.168.80.138";
        int msgNum = 3*1000*1000;
        msgNum = 1;
        int msgLength = 100;
        if(args!=null && args.length>1){
            port = Integer.parseInt(args[1]);
            host = args[0];
            if(args.length>2){
                msgNum = Integer.parseInt(args[2]);
            }
            if(args.length>3){
                msgLength = Integer.parseInt(args[3]);
            }
        }

        connect4test(port, host, msgNum, msgLength,null);


    }

    public static void connect4test(int port, String host, int msgNum, int msgLength, BiConsumer<ByteBuf,Integer> consumer) throws InterruptedException {
        NettyTcpClient client = new NettyTcpClient(host,port);
        CountDownLatch c = new CountDownLatch(1);
        AtomicInteger msgCounter = new AtomicInteger();
        int finalMsgNum = msgNum;
        client.setChildHandler(
                new ChildChannelHandler((ch)->{
                    ch.pipeline().addLast("idleStateHandler",new IdleStateHandler(5,5,5)).addLast(newLengthFieldBasedFrameDecoder()).addLast(

                            new InputHandler((ctx, msg) -> {
                                ByteBuf m = (ByteBuf) msg;
                                int recieverMsgs = msgCounter.incrementAndGet();
                                if(m.getInt(0)==-1){
                                    printMsgInfo(m);
                                }
                                if(recieverMsgs%1000000==0){
                                    System.out.print(System.currentTimeMillis()+"\treply messages :"+recieverMsgs+"\t");
                                    printMsgInfo(m);
                                }
                                if(consumer != null){
                                    consumer.accept(m,recieverMsgs);
                                }
                                if(recieverMsgs ==finalMsgNum){
//                            printMsgInfo(m);
                                    System.out.println("completed");
                                    c.countDown();
                                }

                                m.release();
                            }).setOnChannelInactive(ctx->{
                                ctx.channel().eventLoop().schedule(()->{
                                    reconnect(client);
                                },1,TimeUnit.SECONDS);

                            }).setOnUserEventTriggered((ctx,evt)->{
                                if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
                                    IdleStateEvent event = (IdleStateEvent) evt;
                                    if (event.state() == IdleState.READER_IDLE) {
//                                        System.out.println("read idle");
                                    }else if (event.state() == IdleState.WRITER_IDLE) {
//                                        System.out.println("write idle");
                                    }else if (event.state() == IdleState.ALL_IDLE) {
                                        byte[] bytes = "ping".getBytes();
                                        ByteBuf msgBuf = Unpooled.buffer(bytes.length + 8).writeInt(bytes.length + 4).writeInt(-1).writeBytes(bytes);
                                        client.send(msgBuf);
                                    }
                                }})
                    );})

        );
        Channel connect = client.connect();

        if(connect == null){
            return;
        }






        String msg = new Random().ints(0,10).limit(msgLength).collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
        byte[] bytes = msg.getBytes();
        System.out.println(bytes.length+"\t"+msg);

        long start = System.currentTimeMillis();
        for (int i = 0; i < msgNum; i++) {
            ByteBuf msgBuf = Unpooled.buffer(bytes.length+8).writeInt(bytes.length+4).writeInt(i+1).writeBytes(bytes);
            connect.write(msgBuf);
            if(i%10000==0){
                if(i%1000000==0){
                    System.out.println(System.currentTimeMillis()+"\tsend messages :"+i);
                }
                connect.flush();
                while(!connect.isWritable()){
                }
            }
        }
        connect.flush();
        c.await();

        long end = System.currentTimeMillis();
        long cost = end -start;
        System.out.println("cost:"+cost+"\tqps:"+msgNum*1000L/cost);
    }

    private static void reconnect(NettyTcpClient client) {
        Channel connect = client.connect();
        LOG.info("reconnecting:"+connect);
    }

    private static void printMsgInfo(ByteBuf m) {
        int seq = m.readInt();
        int len = m.readableBytes();
        byte[] content = new byte[len];
        m.readBytes(content);
        System.out.println("msg length:"+ len +"\t seq:"+seq+"\t content:"+ new String(content));
    }
}
