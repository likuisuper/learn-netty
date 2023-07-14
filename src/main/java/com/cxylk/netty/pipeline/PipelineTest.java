package com.cxylk.netty.pipeline;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * @author likui
 * @description
 * @date 2023/7/14 16:23
 **/
public class PipelineTest {
    @Test
    public void test1() throws IOException {
        NioDatagramChannel channel = new NioDatagramChannel();
        ChannelFuture channelFuture = new NioEventLoopGroup(1).register(channel);
        channelFuture.addListener(future -> System.out.println("注册完成"));

        //第二个出站事件中将信息发送到UDP，必须要先绑定一个端口，这个端口和发送的端口没关系，不绑定就发送不了
        channel.bind(new InetSocketAddress(8081));
        //第一个入站事件
        channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                System.out.println("入站事件1:" + msg);
                String message = (String) msg;
                //传递到下一个节点
                ctx.fireChannelRead(message += " hahaha");
                //当然，我们也可以调用父类的channelRead方法，它默认就会调用ctx.fireChannelRead(msg);
                //super.channelRead(ctx,message+" hahaha");
            }
        });
        //第一个出站事件
        channel.pipeline().addLast(new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                System.out.println("出站事件:" + msg.toString());
                ctx.write(msg.toString() + " love netty");
            }
        });
        //第二个出站事件，要添加到头部，因为出站事件是从尾部开始往头部处理的
        channel.pipeline().addFirst(new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                DatagramPacket packet = new DatagramPacket(Unpooled.wrappedBuffer(msg.toString().getBytes()), new InetSocketAddress("127.0.0.1", 8080));
                ctx.write(packet);
            }
        });
        //第二个入站事件，但是会发现消息没有打印
        //原因是上一个channelHandler处理完后就不再先下传递了，需要上一个handler调用fireChannelRead方法手动进行触发
        channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                System.out.println("入站事件2:" + msg);
            }
        });
        //触发入站消息
        //channel.pipeline().fireChannelRead("hello netty");
        //触发出站消息,写入是写入缓冲区，必须刷新
        channel.pipeline().writeAndFlush("lk");
        System.in.read();
    }

    @Test
    public void open() throws IOException {
        NioDatagramChannel datagramChannel = new NioDatagramChannel();
        datagramChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                System.out.println(msg);
                DatagramPacket packet = (DatagramPacket) msg;
                System.out.println(packet.content().toString(Charset.defaultCharset()));
            }
        });
        NioEventLoopGroup group = new NioEventLoopGroup();
        group.register(datagramChannel);
        datagramChannel.bind(new InetSocketAddress(8080));
        System.in.read();
    }
}
