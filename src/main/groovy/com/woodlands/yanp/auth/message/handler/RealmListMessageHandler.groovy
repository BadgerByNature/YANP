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
//file:noinspection GrMethodMayBeStatic
package com.woodlands.yanp.auth.message.handler

import com.woodlands.yanp.auth.AuthAttributeKey
import com.woodlands.yanp.auth.AuthCommand
import com.woodlands.yanp.auth.db.entity.RealmEntity
import com.woodlands.yanp.auth.db.repository.RealmCharactersRepository
import com.woodlands.yanp.auth.db.repository.RealmRepository
import com.woodlands.yanp.auth.message.AuthMessage
import com.woodlands.yanp.auth.message.RealmListMessage
import com.woodlands.yanp.auth.model.BuildInfo
import com.woodlands.yanp.common.data.PacketDataWriter
import com.woodlands.yanp.common.network.ByteBufWowPacket
import groovy.util.logging.Slf4j
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import org.springframework.stereotype.Service

@Slf4j
@Service
class RealmListMessageHandler implements AuthMessageHandler {

    final static int MAX_REALM_ZONES = 38 // Max timezone value for a realm in WotLK

    final RealmCharactersRepository realmCharactersRepository
    final RealmRepository realmRepository

    RealmListMessageHandler(RealmCharactersRepository realmCharactersRepository, RealmRepository realmRepository) {
        this.realmCharactersRepository = realmCharactersRepository
        this.realmRepository = realmRepository
    }

    @Override
    void handle(AuthMessage message, Channel ch) {
        log.debug('Handling RealmListMessage')

        def account = ch.attr(AuthAttributeKey.ACCOUNT).get()
        if (!account) {
            throw new Exception('Account not found on Realm List message')
        }

        // Get the character counts for all realms up front instead of opening a query for each realm individually
        def realmCharacters = realmCharactersRepository.findByAccountId(account.id)
        // TODO Realm list cache instead of calling the table on every access
        def realms = realmRepository.findAll()

        def realmInfosWriter = new PacketDataWriter()
        for (RealmEntity realm : realms) {
            // Players can't see any realms with increased security levels
            if (account.gmLevel == 0 && realm.allowedSecurityLevel > 0) continue
            // GMs can apparently see realms at a higher security level than themselves in the list, but these realms show as locked
            // TODO Testing this seems to not work very well. Keeps not fully connecting to the world server, world server seems to keep crashing
            // Should investigate later, might be an important feature for development purposes
            def locked = realm.allowedSecurityLevel > account.gmLevel
            // Get the character count for this account at this specific realm
            def characterCount = realmCharacters.find { it.realmId == realm.id }.with { it?.count ?: 0 }

            realmInfosWriter.writeByte(realm.icon)
            realmInfosWriter.writeByte(locked)
            realmInfosWriter.writeByte(realm.realmFlags)
            realmInfosWriter.writeNullTerminatedAsciiString(realm.name)
            realmInfosWriter.writeNullTerminatedAsciiString("$realm.address:$realm.port")
            realmInfosWriter.writeFloatLE(realm.population)
            realmInfosWriter.writeByte(characterCount)
            realmInfosWriter.writeByte(getRealmZoneFromTimezone(realm.timezone)) // This is really the realm zone or realm category, e.g. US or Oceanic
            realmInfosWriter.writeByte(0x2c) // CMangos just always writes 0x2c. ACore writes field[0]'s id, which sounds like the database id - that seems like info the client should not see
            if (realm.realmFlags & 0x04) {
                BuildInfo buildInfo
                Integer clientBuild = ch.attr(AuthAttributeKey.BUILD).get()
                def realmBuilds = realm.realmBuilds.split(' ') // Realm builds are space-separated
                if (clientBuild.toString().trim() in realmBuilds) {
                    buildInfo = BuildInfo.BUILDS.get(clientBuild)
                } else {
                    buildInfo = BuildInfo.BUILDS.get(Integer.valueOf(realmBuilds[0].trim()))
                }
                if (buildInfo) {
                    realmInfosWriter.writeByte(buildInfo.majorVersion) // Realm major version
                    realmInfosWriter.writeByte(buildInfo.minorVersion) // Realm minor version
                    realmInfosWriter.writeByte(buildInfo.bugfixVersion) // Patch version
                    realmInfosWriter.writeShortLE(clientBuild) // Build number/identifier, 8606 for TBC 2.4.3
                }
            }
        }
        realmInfosWriter.writeShortLE(0x0010)

        def payloadWriter = new PacketDataWriter()
        /* We write the packet size up front, and it is equal to the size of the realmInfos content,
           plus a seemingly-unused int (4 bytes), plus a short representing number of realms sent (2 bytes) */
        payloadWriter.writeShortLE(realmInfosWriter.getBytes().length + 4 + 2) // Payload size as short
        payloadWriter.writeIntLE(0) // Unused int (4 bytes)
        payloadWriter.writeShortLE(countEligibleRealms(account.gmLevel, realms)) // Total eligible realm count short (2 bytes) - may not match realms.size
        payloadWriter.write(realmInfosWriter.getBytes()) // All the realm information

        ch.writeAndFlush(new ByteBufWowPacket(
                opCode: AuthCommand.CMD_REALM_LIST.code,
                payload: Unpooled.wrappedBuffer(payloadWriter.bytes))
        )
    }

    @Override
    boolean handles(AuthMessage message) {
        message instanceof RealmListMessage
    }

    /**
     * Returns the realm zone from the value stored in the account `timezone` field. The logic for this varies by
     * expansion. A realm zone represents a sort of region, such as US, German, or Oceanic
     *
     * @param timezone
     * @return realmZone identifier for the given timezone
     */
    int getRealmZoneFromTimezone(int timezone) {
        if (timezone > MAX_REALM_ZONES) return 1
        // Vanilla would have some special handling here. Wrath just uses the timezone values (if <= MAX_REALM_ZONE)
        // TBC uses the timezone value up until entry 31
        // Also 'timezone' isn't accurate, it's more Realm Zone, e.g. United States, or Oceanic
        timezone in 0..30 ? timezone : 0
    }

    /**
     * Returns the number of realms this account is eligible for. This may not match the number of accounts
     * sent to the client. GMs can apparently see realms with higher required security, but they show as locked
     * and are not counted towards 'eligible' realms
     *
     * @param gmLevel securityLevel of the account, also called gmLevel
     * @param realmList the list of realms
     * @return count of realms this user can log into in the realmList
     */
    int countEligibleRealms(int gmLevel, List<RealmEntity> realmList) {
         realmList.count { gmLevel >= it.allowedSecurityLevel }
    }
}
