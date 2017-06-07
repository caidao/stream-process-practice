package com.paner.netty.time_protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * Created by paner on 17/6/7.
 */
public class TimeEncoder extends ChannelOutboundHandlerAdapter{

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        UnixTime m = (UnixTime) msg;
        ByteBuf encoded  = ctx.alloc().buffer(4);
        encoded.writeInt((int) m.value());
        ctx.write(encoded,promise);
    }
}
