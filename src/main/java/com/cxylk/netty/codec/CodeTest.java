package com.cxylk.netty.codec;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;

/**
 * @author likui
 * @description
 * @date 2023/7/24 9:49
 **/
public class CodeTest {
    private ServerBootstrap bootstrap;

    @Before
    public void init(){
        bootstrap=new ServerBootstrap();
        bootstrap.group(new NioEventLoopGroup(1),new NioEventLoopGroup(8));
        bootstrap.channel(NioServerSocketChannel.class);
    }

    @Test
    public void start() throws InterruptedException {
        bootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                System.out.println("管道注册成功");
                //固定大小解码器，比如满足5个长度才读取消息
                //ch.pipeline().addLast(new FixedLengthFrameDecoder(5));
                //换行解码器,遇到\r\n和\n才读取消息，消息大小不能超过10
                //ch.pipeline().addLast(new LineBasedFrameDecoder(10));
                ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[]{'$'});
                //分隔符解码器，true表示去掉分隔符，那么输出的消息就不会出现我们指定的分隔符
                //ch.pipeline().addLast(new DelimiterBasedFrameDecoder(10,true,byteBuf));
                ch.pipeline().addLast(new TrackHandler());
            }
        });
        ChannelFuture channelFuture = bootstrap.bind(8080).sync();
        channelFuture.channel().closeFuture().sync();
    }

    public class TrackHandler extends SimpleChannelInboundHandler{
        int count=0;
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf byteBuf= (ByteBuf) msg;
            String message = byteBuf.toString(Charset.defaultCharset());
            //在下面这行代码打个断点等待多条消息阻塞才能测试出TCP拆包、粘包的效果
            System.out.println(String.format("消息%s:%s",++count,message));
        }
    }
}
