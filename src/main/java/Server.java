import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.concurrent.Executors;

/**
 * @author zzzz
 * @create 2019-07-20 下午8:41
 */
public class Server {
    private static final String IP="127.0.0.1";
    private static final int PORT=8081;
    public void serverStart() throws InterruptedException {
        //配置服务端NIO线程组,用于处理注册到本线程上多路复用器selector上的Channel，selector的轮询操作由绑定的EventLoop的run方法驱动
        EventLoopGroup bossGroup=new NioEventLoopGroup(0x1, Executors.newCachedThreadPool());
        EventLoopGroup workGroup=new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()*0x3,Executors.newCachedThreadPool());

        try{
            ServerBootstrap bootstrap=new ServerBootstrap();
            bootstrap.group(bossGroup,workGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new HttpResponseEncoder());
                    socketChannel.pipeline().addLast(new HttpRequestDecoder());
                    socketChannel.pipeline().addLast(new HttpObjectAggregator(65535 ));
                    socketChannel.pipeline().addLast(new ChunkedWriteHandler());
//                    socketChannel.pipeline().addLast(new TestHandler());
                    socketChannel.pipeline().addLast(new MathHandler());

                }
            }).option(ChannelOption.SO_BACKLOG,1024)//设置阻塞方式，最大阻塞长度为1024
                    .childOption(ChannelOption.SO_KEEPALIVE,true);//启动心跳机制
            ChannelFuture future=bootstrap.bind(IP,PORT).sync();//绑定端口，同步等待成功
            System.out.println("服务端启动成功...");
            future.channel().closeFuture().sync();//等待服务器监听窗口关闭
        }finally {
            workGroup.shutdownGracefully();//优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
        }

    }
}

class test{
    public static void main(String[] args) throws InterruptedException {
        Server server=new Server();
        server.serverStart();
    }
}
