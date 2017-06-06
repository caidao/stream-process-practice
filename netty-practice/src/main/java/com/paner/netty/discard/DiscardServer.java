package com.paner.netty.discard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;



/**
 * Created by paner on 17/6/6.
 */
public class DiscardServer {

    private int port;

    public DiscardServer(int port){
        this.port = port;
    }

    public void run() throws InterruptedException {
        //NioEventLoopGroup是处理I/O操作的多线程事件循环
        //实现的是服务器端应用程序，因此将使用两个NioEventLoopGroup。 第一个通常称为“boss”，接受传入连接
        //第二个通常称为“worker”，当“boss”接受连接并且向“worker”注册接受连接，则“worker”处理所接受连接的流量。
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            //ServerBootstrap是一个用于设置服务器的助手类
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup)
                    //指定使用NioServerSocketChannel类，该类用于实例化新的通道以接受传入连接
                    .channel(NioServerSocketChannel.class)
                    //此处指定的处理程序将始终由新接受的通道计算。
                    // ChannelInitializer是一个特殊的处理程序，用于帮助用户配置新的通道
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //很可能要通过添加一些处理程序(例如DiscardServerHandler)来配置新通道的ChannelPipeline来实现您的网络应用程序
                            socketChannel.pipeline().addLast(new DiscardServerHandler());
                        }
                    })
                    //设置指定Channel实现的参数
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true);

            //绑定端口，并开始接受连接
            ChannelFuture f = bootstrap.bind(port).sync();
            f.channel().closeFuture().sync();
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new DiscardServer(8081).run();
    }
}
