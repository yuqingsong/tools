package com.lotusyu.net.tcp.netty.server;

import com.lotusyu.net.tcp.netty.client.NettyTcpClient;
import com.lotusyu.net.tcp.netty.handlers.ChildChannelHandler;
import com.lotusyu.net.tcp.netty.handlers.InputPrinthandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lotusyu.net.tcp.netty.handlers.ChildChannelHandler.newLengthFieldBasedFrameDecoder;

/**
 * NettyTcpServer Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Jul 18, 2018</pre>
 */
public class NettyTcpServerTest {

    static int port = 1234;
    @Before
     public void before() throws Exception {
//        NettyTcpServer server = new NettyTcpServer(port);
//        server.setChildHandler(new ChildChannelHandler().addIntLengthFiled().add(new ChannelInboundHandlerAdapter(){
//            @Override
//            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                ByteBuf m = (ByteBuf) msg;
//                CompositeByteBuf byteBufs = Unpooled.compositeBuffer().writeByte(m.readableBytes()).addComponent(m);
//                ctx.writeAndFlush(byteBufs);
//            }
//
//            @Override
//            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//                cause.printStackTrace();
//            }
//        }));


//        startServer();
    }

    private NettyTcpServer startServer() {

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
        nettyTcpServer.start();
        System.out.println("server started");
        return nettyTcpServer;
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: start()
     */
    @Test
    public void testStart() throws Exception {
        NettyTcpServer server = startServer();

        NettyTcpClient client = new NettyTcpClient(this.port);
        CountDownLatch c = new CountDownLatch(1);
        int msgNum = 100000;
        AtomicInteger msgCounter = new AtomicInteger();
        client.setChildHandler(new ChildChannelHandler((ch)->{
            ch.pipeline().addLast(newLengthFieldBasedFrameDecoder()).addLast(
                    new InputPrinthandler((ctx, msg) -> {
                        ByteBuf m = (ByteBuf) msg;
                        m.release();
                        if(msgCounter.incrementAndGet()==msgNum){
                            System.out.println("completed");
                            c.countDown();
                        }
                    })
            );
        }));
        Channel connect = client.connect();
        String msg = "hello";
        msg = new Random().ints().limit(100).collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
        byte[] bytes = msg.getBytes();

        long start = System.currentTimeMillis();
        for (int i = 0; i < msgNum; i++) {
            ByteBuf msgBuf = Unpooled.buffer(bytes.length+4).writeInt(bytes.length).writeBytes(bytes);
            connect.write(msgBuf);
            if(i%10000==0){
                connect.flush();
            }
        }
        connect.flush();
        c.await();
        long end = System.currentTimeMillis();
        long cost = end -start;
        System.out.println("cost:"+cost+"\tqps:"+msgNum*1000L/cost);
        server.stop();


    }

    /**
     * Method: startAndBlockExit()
     */
    @Test
    public void testStartAndBlockExit() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: startInternal()
     */
    @Test
    public void testStartInternal() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: stop()
     */
    @Test
    public void testStop() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: main(String[] args)
     */
    @Test
    public void testMain() throws Exception {
//TODO: Test goes here... 
    }


} 
