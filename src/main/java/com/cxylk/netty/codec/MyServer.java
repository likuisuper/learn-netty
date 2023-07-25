package com.cxylk.netty.codec;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;

/**
 * @author likui
 * @description 服务端
 * @date 2023/7/24 16:23
 **/
public class MyServer {
    private ServerBootstrap serverBootstrap;

    @Before
    public void init(){
        serverBootstrap=new ServerBootstrap();
        serverBootstrap.group(new NioEventLoopGroup(1),new NioEventLoopGroup(8));
        serverBootstrap.channel(NioServerSocketChannel.class);
    }

    @After
    public void start() throws InterruptedException {
        ChannelFuture future = serverBootstrap.bind(8080);
        System.out.println("服务端启动成功");
        future.sync().channel().closeFuture().sync();
    }

    @Test
    public void test(){
        //服务端引导器必须是childHandler，否则会报错
        serverBootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new MyProtocol());
                ch.pipeline().addLast(new TrackHandler());
            }
        });
    }

    private static class TrackHandler extends SimpleChannelInboundHandler<String> {
        int count=0;
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            //因为在MyProtocol中已经将字节解码成了字符串，所以这里不需要再转换
            System.out.println(String.format("消息%s:%s",count++,msg));
            ctx.writeAndFlush("返回消息");
        }
    }
}
