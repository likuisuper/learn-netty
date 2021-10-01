package com.cxylk.nio.channel;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Classname FileChannelTest
 * @Description 文件管道
 * @Author likui
 * @Date 2021/9/27 20:38
 **/
public class FileChannelTest {
    String file_name="F:\\github\\learn-netty\\test.txt";

    @Test
    public void test() throws IOException {
        //1、创建一个只支持读的文件流，管道是双向的
        //FileInputStream inputStream=new FileInputStream(file_name);

        //获取文件管道
        //FileChannel channel = inputStream.getChannel();

        //2、创建文件管道，文件流是可读可写的（rw）
        FileChannel channel=new RandomAccessFile(file_name,"rw").getChannel();
        //声明1024个字节空间
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        //使用管道从文件中读取数据到指定的缓冲区
        channel.read(byteBuffer);
        //这里一定要调用flip，因为上面的read方法将数据读取到缓冲区
        //其实就是调用了缓冲区的put方法，这样当文件的数据都读取都缓冲区后
        //position的位置就变成了limit，所以下面再读取的话是读取不到remaining的
        byteBuffer.flip();
        //创建一个字节数组接收从缓冲区中读取到的值,大小就是position到limit的长度
        byte[] bytes=new byte[byteBuffer.remaining()];
        int i=0;
        //如果缓冲区有剩余值（position<limit），就读取到字节数组中
        while (byteBuffer.hasRemaining()){
            bytes[i++]=byteBuffer.get();
        }
        System.out.println(new String(bytes));

        //如何使用文件管道往文件中写呢？
        //因为上面的文件流使用的是FileInputStream，只支持读，所以要在上面改为RandomAccessFile
        //否则会报错NonWritableChannelException

        //wrap方法：基于数组包装一个buffer，position=0，limit为容量值
        channel.write(ByteBuffer.wrap("cxylk".getBytes()));
        //关闭管道
        channel.close();
    }
}
