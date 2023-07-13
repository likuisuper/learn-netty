package com.cxylk.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * @author likui
 * @description 通过bootstrap进一步简化操作，实现一个http
 * @date 2023/7/13 9:41
 **/
public class BootstrapTest {
    public void open(int port) {
        //使用serverBootstrap注册、绑定等io事件进一步封装
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup work = new NioEventLoopGroup(8);
        serverBootstrap.group(boss, work)
                //指定要打开的管道，自动进行注册
                .channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                //一个http请求，首先是先解码成一个request，然后进行业务处理，最后编码成一个response返回
                //输入流
                ch.pipeline().addLast("decode", new HttpRequestDecoder());
                //将同一个http请求或响应的多个消息对象变成一个 fullHttpRequest完整的消息对象
                ch.pipeline().addLast("aggregator", new HttpObjectAggregator(65535));
                //ch.pipeline().addLast("servlet",new Myservlet());
                ch.pipeline().addLast("servlet", new Myservlet2());
                //输出流
                ch.pipeline().addFirst("encode", new HttpResponseEncoder());
            }
        });
        //异步操作，EventLoop==>NioServerSocketChannel==>ServerSocketChannel
        ChannelFuture channelFuture = serverBootstrap.bind(port);
        channelFuture.addListener(future -> System.out.println("完成绑定"));
    }

    /**
     * 业务处理
     */
    private static class Myservlet extends SimpleChannelInboundHandler {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            //此时这个msg是什么对象呢？
            //我们在pipeline中添加了decode HttpRequestDecoder,该类的文档中说明了它会将字节流解码为
            //httpRequest和httpResponse

            //这样转换直接报异常：io.netty.handler.codec.http.LastHttpContent$1 cannot be cast to io.netty.handler.codec.http.HttpRequest
            //为什么呢？因为会发两次，一次是请求头request,一次是内容body,第一次发过来，转换没问题，但是第二次进来的body，也就就是content，就转换失败了
            //所以需要分情况处理
            if (msg instanceof HttpRequest) {
                HttpRequest httpRequest = (HttpRequest) msg;
                System.out.println("当前请求：" + httpRequest.uri());
            }
            if (msg instanceof HttpContent) {
                ByteBuf content = ((HttpContent) msg).content();
                OutputStream outputStream = new FileOutputStream("D:\\WorkSpace\\github\\learn-netty\\target\\test.mp4", true);
                content.readBytes(outputStream, content.readableBytes());
                outputStream.close();
            }
            //如果是最后一个content
            if (msg instanceof LastHttpContent) {
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=utf-8");
                response.content().writeBytes("上传完毕".getBytes());
                ChannelFuture channelFuture = ctx.writeAndFlush(response);
                channelFuture.addListener(ChannelFutureListener.CLOSE);
            }


        }
    }

    /**
     * 同时读取request和content
     */
    private static class Myservlet2 extends SimpleChannelInboundHandler {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof FullHttpRequest) {
                FullHttpRequest request = (FullHttpRequest) msg;
                System.out.println("uri:" + request.uri());
                System.out.println(request.content().toString(Charset.defaultCharset()));
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=utf-8");
                response.content().writeBytes("上传完毕".getBytes());
                ChannelFuture channelFuture = ctx.writeAndFlush(response);
                channelFuture.addListener(ChannelFutureListener.CLOSE);
            }


        }
    }


    public static void main(String[] args) throws IOException {
        new BootstrapTest().open(8080);
        System.in.read();
    }

}
