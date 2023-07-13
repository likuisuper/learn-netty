package com.cxylk.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * @author likui
 * @description 使用netty中的eventloop对NioServerSocketChannel操作
 * @date 2023/7/12 14:34
 **/
public class NioServerSocketChannelTest {
    @Test
    public void test() throws IOException {
        NioServerSocketChannel serverSocketChannel = new NioServerSocketChannel();
        //声明一个线程，相当于所有工作都由这个一个线程来完成
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        //将channel注册到group中
        group.register(serverSocketChannel);
        //注册完成后就可以绑定ip和端口了
        serverSocketChannel.bind(new InetSocketAddress(8080));
        //然后我们需要监听连接事件
        serverSocketChannel.pipeline().addLast(new SimpleChannelInboundHandler<Object>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                //此时的msg就是NioSocketChannel
                System.out.println(msg);
                System.out.println("已建立新连接");
                //处理NioSocketChannel
                handleAccept(group, msg);
            }
        });
        System.in.read();
    }

    @Test
    public void test2() throws IOException {
        NioServerSocketChannel serverSocketChannel = new NioServerSocketChannel();
        //将一个线程完成所有工作进行分工，boss线程只负责建立连接
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        //work线程负责读写，不指定线程的话，默认是cpu核心(逻辑核心)*2
        NioEventLoopGroup work = new NioEventLoopGroup();
        //将channel注册到group中
        boss.register(serverSocketChannel);
        //注册完成后就可以绑定ip和端口了，提交任务到eventLoop
        serverSocketChannel.bind(new InetSocketAddress(8080));
        //然后我们需要监听连接事件
        serverSocketChannel.pipeline().addLast(new SimpleChannelInboundHandler<Object>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                //此时的msg就是NioSocketChannel
                System.out.println(msg);
                System.out.println("已建立新连接");
                //处理NioSocketChannel,读写操作都让work线程处理，通过调用next方法不断让下一个线程处理任务
                handleAccept(work, msg);
            }
        });
        System.in.read();
    }

    private void handleAccept(NioEventLoopGroup group, Object msg) {
        NioSocketChannel socketChannel = (NioSocketChannel) msg;
        group.register(socketChannel);
        //读取客户端的数据
        socketChannel.pipeline().addLast(new SimpleChannelInboundHandler<Object>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                //此时的msg是netty中的byteBuf，它是对java.nio中ByteBuffer的封装
                ByteBuf byteBuf = (ByteBuf) msg;
                System.out.println(byteBuf.toString(Charset.defaultCharset()));
            }
        });
    }
}
