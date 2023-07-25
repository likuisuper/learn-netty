package com.cxylk.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author likui
 * @description
 * <pre>
 *     +------------------------+
 *     +标志位     长度      消息 +
 *     +4字节      4字节         +
 *     +-----------------------+
 * </pre>
 * @date 2023/7/24 15:50
 **/
public class MyProtocol extends ByteToMessageCodec<String> {

    /**
     * 标志位
     */
    private static final int MAGIC=0xDADA;

    private static final ByteBuf byteBuf=Unpooled.copyInt(MAGIC);

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
        //编码
        byte[] bytes = msg.getBytes();
        //写入标志位
        out.writeInt(MAGIC);
        //写入长度
        out.writeInt(bytes.length);
        //写入消息
        out.writeBytes(bytes);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List out) throws Exception {
        //找到标志位的位置
        int index = indexOf(in, byteBuf);
        //需要更多的字节
        if (index<0) {
            return;
        }
        //需要更多的字节，标志位长度+长度位长度
        if(!in.isReadable(index+8)){
            return;
        }
        //获取消息长度，因为我们在编码中直接写入了长度，所以readInt获取的就是字节数组中的值也就是长度
        int length = in.slice(index + 4, 4).readInt();
        //缓冲区内的元素是否满足 标志位+长度+消息 的长度
        if(!in.isReadable(index+8+length)){
            return;
        }
        //跳过标志位和长度位
        in.skipBytes(index+8);
        ByteBuf slice = in.readRetainedSlice(length);
        String message = slice.toString(Charset.defaultCharset());
        //底层会调用fireChannelRead将消息传递到下一个handler
        out.add(message);
    }

    /**
     * @see DelimiterBasedFrameDecoder#indexOf(ByteBuf, ByteBuf),在byteBuf中找出指定字符的位置
     * @param haystack
     * @param needle
     * @return
     */
    private static int indexOf(ByteBuf haystack, ByteBuf needle) {
        for (int i = haystack.readerIndex(); i < haystack.writerIndex(); i ++) {
            int haystackIndex = i;
            int needleIndex;
            for (needleIndex = 0; needleIndex < needle.capacity(); needleIndex ++) {
                if (haystack.getByte(haystackIndex) != needle.getByte(needleIndex)) {
                    break;
                } else {
                    haystackIndex ++;
                    if (haystackIndex == haystack.writerIndex() &&
                            needleIndex != needle.capacity() - 1) {
                        return -1;
                    }
                }
            }

            if (needleIndex == needle.capacity()) {
                // Found the needle from the haystack!
                return i - haystack.readerIndex();
            }
        }
        return -1;
    }
}
