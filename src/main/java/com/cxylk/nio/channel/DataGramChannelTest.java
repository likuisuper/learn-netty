package com.cxylk.nio.channel;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * @Classname DataGramChannelTest
 * @Description udp管道测试
 * @Author likui
 * @Date 2023-06-11 18:33
 **/
public class DataGramChannelTest {
    @Test
    public void test1() throws IOException {
        DatagramChannel datagramChannel = DatagramChannel.open();
        //udp不需要建立连接
        datagramChannel.bind(new InetSocketAddress(8088));
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        //读取客户端的数据到buffer中，注意这里是receive不是read
        datagramChannel.receive(buffer);
        //读取完之后flip
        buffer.flip();
        byte[] bytes=new byte[buffer.remaining()];
        //将buffer中的数据读取到字节数组
        buffer.get(bytes);
        System.out.println(new String(bytes));
        //因为调用了get，所以要调用rewind
        buffer.rewind();
        //回写消息到客户端，但是报错，因为udp是单向的，没有连接，不支持回写
        //datagramChannel.write(buffer);
        //只能通过send方法将消息重新发送到另外一个地址
        datagramChannel.send(buffer,new InetSocketAddress("127.0.0.1",8010));
        //关闭管道
        datagramChannel.close();
    }


    @Test
    public void test2() throws IOException {
        DatagramChannel channel=DatagramChannel.open();
        // 绑定端口
        channel.bind(new InetSocketAddress(8088));
        ByteBuffer buffer=ByteBuffer.allocate(8192);

        while (true){
            buffer.clear(); //  清空还原
            channel.receive(buffer); // 阻塞
            buffer.flip();
            byte[] bytes=new byte[buffer.remaining()];
            buffer.get(bytes);
            System.out.println(new String(bytes));
            //因为调用了get，所以要调用rewind
            buffer.rewind();
            //回写消息到客户端，但是报错，因为udp是单向的，没有连接，不支持回写
            //datagramChannel.write(buffer);
            //只能通过send方法将消息重新发送到另外一个地址
            channel.send(buffer,new InetSocketAddress("127.0.0.1",8010));
            //关闭管道
            channel.close();
        }
    }

    /**
     * selector功能demo
     */
    @Test
    public void demo() throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        channel.bind(new InetSocketAddress(8081));
        channel.configureBlocking(false);
        Selector selector=Selector.open();
        channel.register(selector,SelectionKey.OP_READ);
//        ByteBuffer buffer=ByteBuffer.allocate(1024);
//        channel.receive(buffer);
    }

    /**
     * 非阻塞模式
     */
    @Test
    public void test3() throws IOException {
        DatagramChannel channel=DatagramChannel.open();
        // 绑定端口
        channel.bind(new InetSocketAddress(8088));
        //注册到选择器之前一定要设置阻塞模式，false表示非阻塞
        channel.configureBlocking(false);
        //打开选择器
        Selector selector=Selector.open();
        //将管道注册到选择器，关注的事件为read操作
        channel.register(selector, SelectionKey.OP_READ);
        while (true){
            //select刷新键集，它是阻塞的，知道有关注的事件进来，这里是READ事件
            int count = selector.select();
            if(count>0){
               //表示有指定的数量的键状态发生了变更
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    handler(selectionKey);
                }
            }
        }
    }

    private void handler(SelectionKey selectionKey) throws IOException {
        //通过SelectionKey获取管道，SelectionKey用于关联channel和selector
        DatagramChannel channel = (DatagramChannel) selectionKey.channel();
        ByteBuffer buffer=ByteBuffer.allocate(1024);
        //此时receive不再是阻塞的
        channel.receive(buffer);
        buffer.flip();
        byte[] bytes=new byte[buffer.remaining()];
        buffer.get(bytes);
        System.out.println(new String(bytes));
    }

}
