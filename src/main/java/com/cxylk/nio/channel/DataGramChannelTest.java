package com.cxylk.nio.channel;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

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
}
