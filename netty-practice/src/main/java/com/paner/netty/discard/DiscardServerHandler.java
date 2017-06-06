package com.paner.netty.discard;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by paner on 17/6/6.
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //super.channelRead(ctx, msg);
        ByteBuf in = (ByteBuf) msg;
        try{
            while (in.isReadable()){
                System.out.print((char)in.readByte());
                System.out.flush();
            }
        }finally {
           in.release();
        }
        System.out.println("ctx = [" + ctx.name() + "], msg = [" + msg + "]");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
