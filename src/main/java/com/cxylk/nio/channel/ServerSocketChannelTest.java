package com.cxylk.nio.channel;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;

/**
 * @Classname ServerSocketChannelTest
 * @Description TCP套接字管道
 * @Author likui
 * @Date 2021/10/1 10:56
 **/
public class ServerSocketChannelTest {
    /**
     * 只能发送一个消息
     * @throws IOException
     */
    @Test
    public void  test() throws IOException {
        //ServerSocketChannel用于与客服端建立连接
        //1、打开TCP服务管道
        ServerSocketChannel channel=ServerSocketChannel.open();
        //2、绑定端口
        channel.bind(new InetSocketAddress(8080));
        //3、接收客服端发送的连接请求，如果没有则阻塞
        SocketChannel socketChannel = channel.accept();
        ByteBuffer buffer=ByteBuffer.allocate(1024);
        //4、读取客服端发来的消息，如果没有则阻塞
        socketChannel.read(buffer);
        //read就是读取客户端的数据写入到buffer中，底层调用的是buffer的put方法，所以一定要flip，让position重新从0开始，flip常用于put之后，至于为什么？想想就明白了
        buffer.flip();
        byte[] bytes=new byte[buffer.remaining()];
        //将缓冲区的数据读取到字节数组
        buffer.get(bytes);
        System.out.println(new String(bytes));

        //5、回写消息
        //上面调用了buffer的get方法，所以在调用write之前要先调用rewind，rewind常用于write或get之前
        buffer.rewind();
        //write就是将服务端的数据写入到buffer中
        socketChannel.write(buffer);
        //6、关闭管道
        socketChannel.close();
        channel.close();
    }

    /**
     * 发送多个消息，但还是只能建立一个连接（可以开启两个端口验证）
     * @throws IOException
     */
    @Test
    public void  test2() throws IOException {
        //ServerSocketChannel用于与客服端建立连接
        //1、打开TCP服务管道
        ServerSocketChannel channel=ServerSocketChannel.open();
        //2、绑定端口
        channel.bind(new InetSocketAddress(8080));
        //3、接收客服端发送的连接请求，如果没有则阻塞
        SocketChannel socketChannel = channel.accept();
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        while (true) {
            //缓冲区重用之前需要clear
            buffer.clear();
            //4、读取客服端发来的消息，如果没有则阻塞
            socketChannel.read(buffer);
            //read就相当于往buffer中put数据，所以一定要flip，让position重新从0开始，flip常用于put之后，至于为什么？想想就明白了
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            //将缓冲区的数据读取到字节数组
            buffer.get(bytes);
            String message = new String(bytes);
            System.out.print(message);
            //5、回写消息
            //上面调用了buffer的get方法，所以在调用write之前要先调用rewind，rewind常用于write或get之前
            buffer.rewind();
            //write就是将服务端的数据写入到buffer中
            socketChannel.write(buffer);
            if(message.trim().equals("q")){
                break;
            }
        }
        //6、关闭管道
        socketChannel.close();
        channel.close();
    }

    /**
     * 可以建立多个连接，BIO的简易模型
     * @throws IOException
     */
    @Test
    public void test3() throws IOException {
        //ServerSocketChannel用于与客服端建立连接
        //1、打开TCP服务管道
        ServerSocketChannel channel=ServerSocketChannel.open();
        //2、绑定端口
        channel.bind(new InetSocketAddress(8080));
        //3、接收客服端发送的连接请求，如果没有则阻塞
        //这里将相当于tomcat的BIO模型中的Acceptor
        while (true){
            handle(channel.accept());
        }
    }

    /**
     * 每来一个连接便分配一个线程处理，这个就是BIO的模型
     * @param socketChannel
     */
    public void handle(SocketChannel socketChannel) throws IOException {
//        //不管是read还是write都是阻塞的，如果想变成非阻塞，采用下面的方法构建一个选择器来托管管道
//        Selector selector=Selector.open();
//        //选择器会监听当前的read操作，当数据读取好了之后，再通知管道
//        //只有继承了SelectableChannel的类才能register
          //将管道注册到选择器之前，必须设置管道为非阻塞模式
//          socketChannel.configureBlocking(false);
//        socketChannel.register(selector, SelectionKey.OP_READ);//非阻塞
        //tomcat的BIO模型采用线程池分配线程，这里直接new线程
        Thread thread=new Thread(()->{
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            while (true) {
                try {
                    //缓冲区重用之前需要clear
                    buffer.clear();
                    //4、读取客服端发来的消息，如果没有则阻塞
                    socketChannel.read(buffer);
                    //read就相当于往buffer中put数据，所以一定要flip，让position重新从0开始，flip常用于put之后，至于为什么？想想就明白了
                    buffer.flip();
                    byte[] bytes = new byte[buffer.remaining()];
                    //将缓冲区的数据读取到字节数组
                    buffer.get(bytes);
                    String message = new String(bytes);
                    System.out.print(message);
                    //5、回写消息
                    //上面调用了buffer的get方法，所以在调用write之前要先调用rewind，rewind常用于write或get之前
                    buffer.rewind();
                    //write就是将服务端的数据写入到buffer中
                    socketChannel.write(buffer);
                    if(message.trim().equals("q")){
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                //6、关闭管道，连接结束后才关闭
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }
}

