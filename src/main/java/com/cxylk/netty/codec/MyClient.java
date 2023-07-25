package com.cxylk.netty.codec;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

/**
 * @author likui
 * @description 客户端
 * @date 2023/7/24 16:17
 **/
public class MyClient {
    private Bootstrap bootstrap;
    private Channel channel;

    public void start() throws InterruptedException {
        bootstrap=new Bootstrap();
        bootstrap.group(new NioEventLoopGroup(1));
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                System.out.println("客户端连接成功");
                ch.pipeline().addLast(new MyProtocol());
            }
        });
        ChannelFuture future = bootstrap.connect(new InetSocketAddress("127.0.0.1", 8080));
        channel=future.sync().channel();
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        MyClient client=new MyClient();
        client.start();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        while (true){
            String line = bufferedReader.readLine();
            //写入消息
            client.channel.writeAndFlush(line);
        }
    }
}
