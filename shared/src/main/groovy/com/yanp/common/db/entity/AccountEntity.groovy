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
package com.yanp.common.db.entity

import groovy.transform.EqualsAndHashCode
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

import java.time.LocalDateTime

@Entity
@EqualsAndHashCode
@Table(name = 'account', catalog = 'tbcrealmd')
class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = 'ID', nullable = false)
    Integer id

    @Column(name = 'USERNAME', length = 32, unique = true)
    String username

    /** Access level of account 0 = regular user, > 0 = GM. */
    @Column(name = 'GMLEVEL')
    Integer gmLevel

    /** The SRP6 session key. Updated each login */
    @Column(name = 'SESSIONKEY')
    String sessionKey

    /** The SRP6 verifier value */
    @Column(name = 'V', nullable = false)
    String v

    /** The SRP6 salt value */
    @Column(name = 'S', nullable = false)
    String s

    @Column(name = 'EMAIL')
    String email

    @Column(name = 'JOINDATE')
    LocalDateTime joinDate

    @Column(name = 'LOCKEDIP', length = 30)
    String lockedIp

    @Column(name = 'FAILED_LOGINS')
    Long failedLogins

    @Column(name = 'LOCKED')
    Boolean locked
    
    @Column(name = 'ACTIVE_REALM_ID')
    Long activeRealmId

    @Column(name = 'EXPANSION')
    Integer expansion

    /** Unix time when an account will become unmuted */
    @Column(name = 'MUTETIME')
    Long mutedUntil

    /** Locale Id.
     *  @see com.yanp.common.constant.LocalizationId */
    @Column(name = 'LOCALE')
    String localeId

    /** 2-Factor auth token */
    @Column(name = 'TOKEN')
    String token

    /** Operating System, e.g. Win/Mac */
    @Column(name = 'OS')
    String os

    /** Platform, e.g. x86 */
    @Column(name = 'PLATFORM')
    String platform

    @Column(name = 'flags')
    Integer flags
}
