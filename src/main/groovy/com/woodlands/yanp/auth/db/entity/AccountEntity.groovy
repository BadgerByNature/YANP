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

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

import java.time.LocalDateTime

@Entity
@Table(name = 'account', catalog = 'tbcrealmd')
class AccountEntity {

    @JsonProperty
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = 'ID', nullable = false)
    Long id

    @JsonProperty
    @Column(name = 'USERNAME', length = 32, unique = true)
    String username

    /** Access level of account 0 = regular user, > 0 = GM. */
    @JsonProperty
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

    @JsonProperty
    @Column(name = 'EMAIL')
    String email

    @JsonProperty
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

    @JsonProperty
    @Column(name = 'EXPANSION')
    Integer expansion

    /** Unix time when an account will become unmuted */
    @Column(name = 'MUTETIME')
    Long mutedUntil

    /** Locale Id.
     *  @see com.woodlands.yanp.common.constant.LocalizationId */
    @JsonProperty
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

    @Override
    int hashCode() {
        return Objects.hash(this.id, this.username)
    }

    @Override
    boolean equals(final Object obj) {

        if (this == obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (getClass() != obj.getClass()) {
            return false
        }
        final AccountEntity other = (AccountEntity) obj
        return Objects.equals(this.id, other.id) && Objects.equals(this.username, other.username)
    }
}
