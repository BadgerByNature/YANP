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
package com.woodlands.yanp.auth.db.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = 'account_banned', catalog = 'tbcrealmd')
class AccountBannedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = 'id', nullable = false)
    Integer id

    @Column(name = 'account_id', nullable = false)
    Integer accountId

    @Column(name = 'banned_at', nullable = false)
    Long bannedAt

    @Column(name = 'expires_at', nullable = false)
    Long expiresAt

    @Column(name = 'banned_by')
    String bannedBy

    @Column(name = 'unbanned_at')
    Long unbannedAt

    @Column(name = 'unbanned_by')
    String unbannedBy

    @Column(name = 'reason')
    String reason

    @Column(name = 'active')
    Boolean isActive
}
