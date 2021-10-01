package com.cxylk.nio.buffer;

import org.junit.Test;

import java.nio.IntBuffer;
import java.util.Arrays;

/**
 * @Classname IntBufferTest
 * @Description 缓冲区测试
 * @Author likui
 * @Date 2021/9/23 22:29
 **/
public class IntBufferTest {
    @Test
    public void test1(){
        //position：位置，当前读写位置，默认是0，每读或写一个位就会加1
        //limit：限制，能够进行读写的最大值，必须小于或等于capacity
        //capacity：缓冲区容量，即内部数组的大小，一旦声明就不允许改变
        //声明一个初始值都为0的int类型的缓冲区，大小为6个空间，position=0，limit=capacity=6
        IntBuffer intBuffer=IntBuffer.allocate(6);
        //往缓冲区填充数据
        intBuffer.put(1);
        intBuffer.put(2);
        //获取此时position的位置，结果是2
        System.out.println(intBuffer.position());
        intBuffer.get();
        intBuffer.get();
        intBuffer.get();
        intBuffer.get();
        //此时position为6
        System.out.println(intBuffer.position());
        //当在对缓冲区进行读取时，position已经超过了limit，会报BufferUnderflowException异常
        //System.out.println(intBuffer.get());

        //解决办法:
        //调用flip方法，让limit=position，position=0
        intBuffer.flip();//position=0,limit=6
        //此时会从position=0的位置开始读取，结果为1
        System.out.println(intBuffer.get());
        //当前也可以调用clear方法，让position=0,limit=capacity，相当于回到初始状态
        //intBuffer.clear();

        //此时position=1，继续读取
        intBuffer.get();
        intBuffer.get();
        //此时position=3
        System.out.println(intBuffer.position());
        //调用flip，让limit=3，position=0
        intBuffer.flip();
        System.out.println(intBuffer.limit());
        //调用put写入数据
        intBuffer.put(1);
        intBuffer.put(2);
        intBuffer.put(3);
        //此时position=3，再调用put的话，position=4，超过了limit=3，会报BufferOverflowException异常
        //intBuffer.put(4);

        //调用clear方法，回到初始状态
        intBuffer.clear();
        intBuffer.put(1);
        intBuffer.put(2);
        intBuffer.put(3);
        //此时position=3，调用mark方法，让mark=position
        intBuffer.mark();
        //mark用来替换一段内容
        int i = intBuffer.get() + 100;
        int i1=intBuffer.get()+200;
        System.out.println(Arrays.toString(intBuffer.array()));//[1,2,3,0,0,0]
        //此时position=5
        System.out.println(intBuffer.position());
        //调用reset让position回到mark的位置3
        intBuffer.reset();
        System.out.println(intBuffer.position());
        intBuffer.put(i);
        intBuffer.put(i1);
        System.out.println(Arrays.toString(intBuffer.array()));
    }
}
