package com.cxylk.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
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
 * @date 2023/7/11 20:05
 **/
public class EventLoopTest {
    @Test
    public void test() {
        //当指定一个线程后，下面两个方法打印的线程id都是相同的
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        //execute方法立即执行
        group.execute(() -> System.out.println("execute:" + Thread.currentThread().getId()));
        group.submit(() -> System.out.println("submit:" + Thread.currentThread().getId()));
        //优雅关闭
        group.shutdownGracefully();
    }

    @Test
    public void test2() throws IOException {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        NioDatagramChannel channel = new NioDatagramChannel();
        //一定要先将channel注册到group中，不然调用bind操作就会报错
        group.register(channel);
        //此时bind操作不再由当前线程执行，而是由eventLoop执行，也就是在reactor中完成
        ChannelFuture channelFuture = channel.bind(new InetSocketAddress(8080));
        channelFuture.addListener(future -> System.out.println("完成绑定"));
        //以前使用NIO的时候，我们会在一个循环中调用selector方法监听读写事件
        //而在netty中，需要调用pipeline来添加一个handler处理对应的事件
        channel.pipeline().addLast(new SimpleChannelInboundHandler() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                //msg就是DatagramPacket对象
                System.out.println(msg);
                //注意，这个DatagramPacket是netty下的包，不是jdk的包
                if (msg instanceof DatagramPacket) {
                    DatagramPacket packet = (DatagramPacket) msg;
                    System.out.println(packet.content().toString(Charset.defaultCharset()));
                }
            }
        });
        System.in.read();
    }
}
