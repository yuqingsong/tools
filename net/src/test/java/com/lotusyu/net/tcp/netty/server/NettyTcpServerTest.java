package com.lotusyu.net.tcp.netty.server;

import com.lotusyu.net.tcp.netty.client.NettyTcpClient;
import com.lotusyu.net.tcp.netty.handlers.ChildChannelHandler;
import com.lotusyu.net.tcp.netty.handlers.InputHandler;
import com.lotusyu.net.tcp.netty.handlers.RateLimitingHandler;
import io.netty.buffer.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.junit.After;
import org.junit.Assert;
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

    private void startServer() {
        NettyTcpServer.startServer4Test(port);
        System.out.println("server started");
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: start()
     */
    @Test
//    @RepeatedTest(10)
    public void testStart() throws Exception {
        startServer();
        int threadNum =0;
        if(threadNum>0){

            CountDownLatch c = new CountDownLatch(threadNum);
            for (int i = 0; i < threadNum; i++) {
                new Thread(()->{
                    try {
                        testSendMsg();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }finally {
                        c.countDown();
                    }
                }).start();
            }
            c.await();
        }else{
            testSendMsg();
        }



    }

    public void testSendMsg() throws InterruptedException {
        String host = "192.168.80.138";
        host = "localhost";
        NettyTcpClient client = new NettyTcpClient(host,this.port);
        CountDownLatch c = new CountDownLatch(1);
        int num = 1*1000 * 1000;
//        num = 3;
        int msgNum = num;
        long start = System.currentTimeMillis();
        NettyTcpClient.connect4test(port,host,num,100,(buf,counter)->{
            if(counter.intValue()==num){
                int seq = buf.getInt(0);
                //校验最后一个消息的序号
                Assert.assertEquals(msgNum,seq);
            }
        });
        long end  = System.currentTimeMillis();
        long cost = end -start;

        long qps = msgNum * 1000L / cost;
        //校验性能
        Assert.assertTrue(qps>100*1000);

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
        ByteBuf b = Unpooled.directBuffer().writeInt('b');
        ByteBuf c = Unpooled.directBuffer().writeInt('c');
        CompositeByteBuf byteBufs = Unpooled.compositeBuffer(2).addComponents(true,b,c);
        System.out.println("first:"+byteBufs.readInt());
        System.out.println("second:"+byteBufs.readInt());
        NettyTcpServer.pringByteBufInfo(byteBufs);
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
