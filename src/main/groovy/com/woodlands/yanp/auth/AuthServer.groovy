package com.woodlands.yanp.auth

import com.woodlands.yanp.auth.decode.AuthByteToMessageDecoderService
import com.woodlands.yanp.auth.encode.AuthResponseEncoder
import com.woodlands.yanp.auth.message.AuthMessage
import groovy.util.logging.Slf4j
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.MultiThreadIoEventLoopGroup
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.util.NetUtil
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * The AuthServer which listens for incoming connections. In some implementations this is called the Realm List
 * or RealmList Server. This is the very first place that a client connects when the Login button is pressed.
 * We validate their credentials against their account and then send information about world servers they may connect to.
 */
@Slf4j
@Service
class AuthServer {

    private final AuthChannelInboundHandler authChannelInboundHandler
    private final AuthResponseEncoder authResponseEncoder
    private final List<AuthCommandDecoder<? extends AuthMessage>> authCommandDecoders
    final int port

    AuthServer(
            AuthChannelInboundHandler authChannelInboundHandler,
            AuthResponseEncoder authResponseEncoder,
            List<AuthCommandDecoder<? extends AuthMessage>> authCommandDecoders,
            @Value('${auth.port:3724}') int port // If we load our config differently we could use the @TupleConstructor for less code here
    ) {
        this.authCommandDecoders = authCommandDecoders
        this.authChannelInboundHandler = authChannelInboundHandler
        this.authResponseEncoder = authResponseEncoder
        this.port = port
    }

    private static final EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory())
    private static final EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory())

    /** Initialize the AuthServer. Runs on PostConstruct */
    @PostConstruct
    void init() {
        ServerBootstrap bootstrap = new ServerBootstrap()
            .group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel)
            .childHandler(new ChannelInitializer() { // An anonymouse ChannelListener should be enough for our needs
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    // Create a new AuthChannelInboundHandler here instead of autowiring
                    // So that we can maintain state per Channel, e.g. account name and login status
                    ch.pipeline().addLast("decoder", new AuthByteToMessageDecoderService(authCommandDecoders))
                    ch.pipeline().addLast(authChannelInboundHandler)
                    ch.pipeline().addLast("encoder", authResponseEncoder)
                }
            })
            // Options for the parent ServerChannel, where we listen for incoming connections
            .option(ChannelOption.SO_BACKLOG, NetUtil.SOMAXCONN) // Max queue length for incoming requests
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            // Options for the child channels, which is one socket per connected client
            .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childOption(ChannelOption.SO_KEEPALIVE, true)

        ChannelFuture cf = bootstrap.bind(port).addListener(f -> {
            if (f.isSuccess()) {
                log.info("Realm Server listening on port $port")
            } else {
                log.error("Binding to port $port failed", f.cause())
            }
        })

        // TODO Do we need to 'await' the bind here? Should we mark ourselves as 'ready' when it's ready? Should we do something with the failure?
        try {
            cf.await()
        } catch (InterruptedException ignored) {
            // Restore interrupted status
            Thread.currentThread().interrupt()
        }
    }
}
