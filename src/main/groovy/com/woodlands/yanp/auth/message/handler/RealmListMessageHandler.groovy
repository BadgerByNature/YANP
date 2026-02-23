/*
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
 *
 * Copyright (c) 2026 YANP: You Are Not Prepared
 * See CONTRIBUTORS.md for further Copyright information
 */
package com.woodlands.yanp.auth.message.handler

import com.woodlands.yanp.auth.AuthCommand
import com.woodlands.yanp.auth.AuthServer
import com.woodlands.yanp.auth.db.entity.RealmEntity
import com.woodlands.yanp.auth.db.repository.RealmRepository
import com.woodlands.yanp.auth.message.AuthMessage
import com.woodlands.yanp.auth.message.RealmListMessage
import com.woodlands.yanp.common.data.PacketDataWriter
import com.woodlands.yanp.common.network.ByteBufWowPacket
import groovy.util.logging.Slf4j
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import org.springframework.stereotype.Service

@Slf4j
@Service
class RealmListMessageHandler implements AuthMessageHandler {

    final RealmRepository realmRepository

    RealmListMessageHandler(RealmRepository realmRepository) {
        this.realmRepository = realmRepository
    }

    @Override
    void handle(AuthMessage message, Channel ch) {
        log.debug('Handling RealmListMessage')

//        def account = ch.attr(AuthServer.ACCOUNT).get() // TODO get info from account once we're satisfied this is working

        // TODO Realm list cache instead of calling the table on every access
        def realms = realmRepository.findAll()
        log.debug('Realm findAll() completed')

        def realmWriter = new PacketDataWriter()
        for (RealmEntity realm : realms) {
            realmWriter.writeByte(realm.icon)
            realmWriter.writeByte(0) // TODO Determine if realm is 'locked' and write it here - Only GMs can see realms that have a higher security level than they can access, those realms display as locked
            realmWriter.writeByte(realm.realmFlags)
            realmWriter.writeNullTerminatedAsciiString(realm.name)
            realmWriter.writeNullTerminatedAsciiString("$realm.address:$realm.port")
            realmWriter.writeFloatLE(realm.population)
            realmWriter.writeByte(5) // TODO Num Characters from realmcharacters table by realmid and acctid (defaulting to 5 for testing)
            realmWriter.writeByte(realm.timezone) // TODO Realm Zone from Build + timezone? See CMangos
            realmWriter.writeByte(0x2c) // TODO TBC sends Realm Id here, Vanilla sends 0x00
            // TODO Get real Build info - not required?
//            realmWriter.writeByte(0x02) // Realm major version
//            realmWriter.writeByte(0x04) // Realm minor version
//            realmWriter.writeByte(0x03) // Patch version
//            realmWriter.writeShortLE(8606) // Build number/identifier
        }
        realmWriter.writeShortLE(0x0010)

        def payloadWriter = new PacketDataWriter()
        payloadWriter.writeShortLE(realmWriter.getBytes().length + 6) // realm buffer size plus unused int and realms count as short (below)
        payloadWriter.writeIntLE(0) // Unused int (4 bytes)
        payloadWriter.writeShortLE(realms.size()) // Realm size short (2 bytes)
        payloadWriter.write(realmWriter.getBytes())
        log.debug("Realm List: ${payloadWriter.bytes.encodeHex()}")
        ch.writeAndFlush(new ByteBufWowPacket(
                opCode: AuthCommand.CMD_REALM_LIST.code,
                payload: Unpooled.wrappedBuffer(payloadWriter.bytes))
        )
    }

    @Override
    boolean handles(AuthMessage message) {
        message instanceof RealmListMessage
    }
}

// TODO Seeing error when this handler loads:
// Obtaining singleton bean 'transactionManager' in thread "multiThreadIoEventLoopGroup-3-1" while other thread holds singleton lock for other beans [authServer]
// Not sure why it's running into a lock, or why it's trying to 'refresh' the authServer bean.
// The handler is still working, but I'd like to resolve the pseudo-error (it's only logged as an INFO but maybe there's a performance bottleneck?)
//
// Googling suggested maybe it's due to blocking on the IO thread, but the error is happening before I even log the 'Handling RealmListMessage'
// May try the following to see what it does
//
// Submit the blocking operation to the custom executor
//        executor.submit(() -> {
//            try {
//                // Perform time-consuming work here (e.g., DB access)
//                Object result = performBlockingOperation(msg);
//                // Write the result back to the Netty pipeline on the event loop
//                ctx.channel().eventLoop().submit(() -> {
//                    ctx.writeAndFlush(result);
//                });
//            } catch (Exception e) {
//                ctx.fireExceptionCaught(e);
//            }
//        });