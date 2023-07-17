package com.cxylk.netty.unsafe;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.AbstractNioChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author likui
 * @description
 * @date 2023/7/17 15:51
 **/
public class UnsafeTest {
    EventLoopGroup loopGroup=new NioEventLoopGroup(1);
    @Test
    public void registerTest() throws IOException {
        NioDatagramChannel channel=new NioDatagramChannel();
        channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
            @Override
            public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                System.out.println("注册成功");
            }
        });
        //直接操作unsafe
        AbstractNioChannel.NioUnsafe unsafe = channel.unsafe();
        unsafe.register(loopGroup.next(),channel.newPromise());
        System.in.read();
    }

    @Test
    public void bindTest() throws IOException {
        NioDatagramChannel channel=new NioDatagramChannel();
        channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                System.out.println(cause.getMessage());
            }

            @Override
            public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                System.out.println("注册成功");
            }

            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                System.out.println("绑定端口成功");
            }
        });
        AbstractNioChannel.NioUnsafe unsafe = channel.unsafe();
        unsafe.register(loopGroup.next(), channel.newPromise());
        //1、直接调用unsafe绑定
        unsafe.bind(new InetSocketAddress(8080), channel.newPromise());
        //2、提交到IO线程中异步绑定
        //channel.eventLoop().submit(()->unsafe.bind(new InetSocketAddress(8080), channel.newPromise()));
        //3、通过channel完成绑定，推荐这种方式
        //channel.bind(new InetSocketAddress(8080));
        System.in.read();
    }
}
