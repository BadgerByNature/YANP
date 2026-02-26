/*
 * Java World of Warcraft Emulation Project
 * Copyright (C) 2015-2020 JavaWoW
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
* Copyright (c) 2026 YANP: You Are Not Prepared
* See CONTRIBUTORS.md for further Copyright information
*/
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
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel)
                    .childHandler(new ChannelInitializer() {
                        // An anonymous ChannelListener should be enough for our needs
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            // Decoder cannot be set to @Shared, but the ChannelInboundHandler and Encoder can be
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
                    log.info("Auth Server listening on port $port")
                } else {
                    log.error("Binding to port $port failed", f.cause())
                }
            })

            cf.channel().closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
    }
}
