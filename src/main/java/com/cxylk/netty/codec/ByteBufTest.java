package com.cxylk.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

/**
 * @author likui
 * @description
 * @date 2023/7/19 9:48
 **/
public class ByteBufTest {
    @Test
    public void rwTest(){
        //声明一个初始容量为5，最大容量为10的字节数组
        ByteBuf buffer = Unpooled.buffer(5,10);
        //不指定最大容量，默认是Integer最大值
        int maxCapacity = buffer.maxCapacity();
        //readIndex:0,writeIndex:1
        buffer.writeByte((byte)1);
        //readIndex:0,writeIndex:2
        buffer.writeByte((byte)2);
        //readIndex:0,writeIndex:3
        buffer.writeByte((byte)3);
        //readIndex:0,writeIndex:4
        buffer.writeByte((byte)4);
        //readIndex:0,writeIndex:5
        buffer.writeByte((byte)5);
        //当超过初始容量后，会进行扩容-->io.netty.buffer.AbstractByteBuf.ensureWritable0-->io.netty.buffer.UnpooledHeapByteBuf.capacity(int)
        //readIndex:0,writeIndex:6
        buffer.writeByte((byte)6);
        //readIndex:1,writeIndex:6
        buffer.readByte();
        //readIndex:2,writeIndex:6
        buffer.readByte();
        buffer.readByte();
        buffer.readByte();
        buffer.readByte();
        //readIndex:6,writeIndex:6
        buffer.readByte();
        //readIndex:7,writeIndex:6
        //报错：IndexOutOfBoundsException
        buffer.readByte();
        //丢弃读索引之前的字节
        //buffer.discardReadBytes();
    }

    @Test
    public void copyTest(){
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[]{1, 2, 3, 4, 5});
        //复制一个视图，内部的byteBuf指向被复制的byteBuf，修改数据会影响原来的byteBuf，但两者具有独立的读写索引
        ByteBuf duplicate = byteBuf.duplicate();
        byteBuf.readByte();
        System.out.println(byteBuf.readerIndex());
        System.out.println(duplicate.readerIndex());
        //赋值全部可读视图区域，和duplicate一样，修改数据会影响原来的byteBuf，读写索引独立
        ByteBuf slice = byteBuf.slice();
        //因为byteBuf已经调用了readByte，所以slice此时的可读区域为4，此时读索引变为0，写索引变为4
        System.out.println(slice.readableBytes());
        //读索引加2
        slice.readSlice(2);
        slice.writerIndex(3);
        //此时字节数字变为1,2,3,4,6  ,byteBuf和duplicate中的字节数组都会改变
        slice.writeByte(6);
        //完全复制一个新的缓冲区，彼此不会影响
        ByteBuf copy = byteBuf.copy();
    }

    @Test
    public void releaseTest(){
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[]{1, 2, 3, 4, 5});
        //从索引1开始，保留2个长度，此时retainedSlice 读索引变为0，写索引变为2，容量变为2
        ByteBuf retainedSlice = byteBuf.retainedSlice(1, 2);
        //读写索引变为0
        byteBuf.clear();
    }
}
