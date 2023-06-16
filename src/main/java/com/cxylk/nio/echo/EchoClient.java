package com.cxylk.nio.echo;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author likui
 * @description 心跳服务客户端。
 * @date 2023/6/14 19:54
 **/
public class EchoClient {
    @Test
    public void clientTest() throws IOException, InterruptedException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        //如果上面设置为同步，那么connect就会去连接，连接不上程序直接退出，是一个阻塞操作
        socketChannel.connect(new InetSocketAddress("127.0.0.1",8080));
        Selector selector = Selector.open();
        //监听事件为connect
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
        while (true){
            //必须调用select刷新，否则connect事件监听不到
            selector.select(100);
            Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
            while (selectionKeys.hasNext()){
                SelectionKey selectionKey = selectionKeys.next();
                selectionKeys.remove();
                //如果未监听到事件
                if(!selectionKey.isValid()){
                    continue;
                }
                //如果感兴趣事件connect已就绪，只要调用了connect方法selector就能监听到该事件，只是此时不一定能完成连接，比如服务端没有启动
                //异步的情况下调用select后就会进入这里
                if(selectionKey.isConnectable()){
                    //未调用finishConnect方法，此时输出的是false
                    System.out.println("是否连接："+socketChannel.isConnected());
                    //因为connect我们已经设置为了异步，所以这里必须要调用该方法完成连接，否则会报未连接异常
                    socketChannel.finishConnect();
                    //将事件变为write
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                }else if(selectionKey.isWritable()){
                    socketChannel.write(ByteBuffer.wrap("heartbeat".getBytes()));
                    //切换事件为read
                    selectionKey.interestOps(SelectionKey.OP_READ);
                }else if(selectionKey.isReadable()){
                    ByteBuffer byteBuffer = ByteBuffer.allocate(64);
                    socketChannel.read(byteBuffer);
                    byteBuffer.flip();
                    System.out.println(new String(byteBuffer.array(),byteBuffer.position(),byteBuffer.limit()));
                    //切换事件为write
                    //这里休眠2s，不要马上写入，否则会打满CPU
                    Thread.sleep(2000);
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                }
            }
        }
    }
}
