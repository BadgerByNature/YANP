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
package com.yanp.auth.db.entity

import groovy.transform.EqualsAndHashCode
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table

@Entity
@IdClass(IpBannedIdClass)
@Table(name = 'ip_banned', catalog = 'tbcrealmd')
class IpBannedEntity {

    @Id
    @Column(name = 'ip', nullable = false)
    String ipAddress

    @Id
    @Column(name = 'banned_at', nullable = false)
    Long bannedAt

    @Column(name = 'expires_at', nullable = false)
    Long expiresAt

    @Column(name = 'banned_by')
    String bannedBy

    @Column(name = 'reason')
    String reason
}

// This uses a composite key of ip and banned_at. My only guess as to why is that we want to keep
// a history of previous bans for an IP
@EqualsAndHashCode
class IpBannedIdClass implements Serializable {
    String ipAddress
    Long bannedAt
}
