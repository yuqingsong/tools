package com.lotusyu.net.tcp.netty.client;

/**
 * @author: yuqingsong
 * @create: 2018-07-19 16:50
 **/

import com.lotusyu.net.tcp.netty.NettyTcpBase;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        // Configure the client.

    }
}
