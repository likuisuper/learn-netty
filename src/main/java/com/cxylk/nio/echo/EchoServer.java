package com.cxylk.nio.echo;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * @author likui
 * @description 心跳服务服务端。
 * @date 2023/6/14 19:31
 **/
public class EchoServer {
    @Test
    public void serverTest() throws IOException {
        //服务端用于接收连接的通道
        ServerSocketChannel serverSocketChannel=ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8080));
        //异步
        serverSocketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        //注册到选择器，监听事件为ACCEPT
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true){
            //一定要调用select进行刷新，不然当客户端触发了accept事件后，这里监听不到
            selector.select(100);
            Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
            while (selectionKeys.hasNext()){
                SelectionKey selectionKey = selectionKeys.next();
                //移除
                selectionKeys.remove();
                //如果未监听到事件
                if(!selectionKey.isValid()){
                    continue;
                }
                //如果感兴趣事件accept已就绪，只要客户端调用connect操作，服务端就能监听到accept事件
                if(selectionKey.isAcceptable()){
                    //连接
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    //将新管道注册到选择器，此时监听的事件为read
                    socketChannel.register(selector,SelectionKey.OP_READ);
                } else if(selectionKey.isReadable()){
                    //获取管道
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    //读取数据
                    ByteBuffer byteBuffer = ByteBuffer.allocate(64);
                    socketChannel.read(byteBuffer);
                    //客户端主动关闭连接，传输结束
                    if (byteBuffer.hasRemaining()&&byteBuffer.get(0)==4) {
                        socketChannel.close();
                        System.out.println("关闭管道:"+socketChannel);
                        break;
                    }
                    //追加时间到缓存尾部
                    byteBuffer.put(String.valueOf(System.currentTimeMillis()).getBytes());
                    byteBuffer.flip();
                    //回写数据到管道
                    socketChannel.write(byteBuffer);
                    byteBuffer.rewind();
                    byte[] bytes=new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);
                    System.out.println(new String(bytes));
                }
            }
        }
    }
}
