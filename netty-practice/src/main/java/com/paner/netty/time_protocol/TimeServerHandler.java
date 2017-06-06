package com.paner.netty.time_protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by paner on 17/6/6.
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter{

    @Override
    public void exceptionCaught(final  ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    //当建立连接并准备好生成流量时，将调用channelActive()方法
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        //要发送新消息，需要分配一个包含消息的新缓冲区。
        // 我们要写入一个32位整数，因此需要一个ByteBuf，其容量至少为4个字节
        final ByteBuf time = ctx.alloc().buffer(4);
        time.writeInt((int)(System.currentTimeMillis()));

        final ChannelFuture f = ctx.writeAndFlush(time);
        f.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) {
                assert f == future;
                ctx.close();
            }
        }); // (4)
    }
}
