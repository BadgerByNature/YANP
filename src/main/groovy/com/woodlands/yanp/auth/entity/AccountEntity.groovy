package com.woodlands.yanp.auth.entity

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
    @Column(name = 'ID', nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id

    @JsonProperty
    @Column(name = 'USERNAME', length = 32, unique = true)
    private String username

    /** Access level of account 0 = regular user, > 0 = GM. */
    @JsonProperty
    @Column(name = 'GMLEVEL')
    private Byte gmLevel

    /** The session key. */
    @Column(name = 'SESSIONKEY')
    private String sessionKey

    /** The v. */
    @Column(name = 'V', nullable = false)
    private String v

    /** The s. */
    @Column(name = 'S', nullable = false)
    private String s

    @JsonProperty
    @Column(name = 'EMAIL')
    private String email

    @JsonProperty
    @Column(name = 'JOINDATE')
    private LocalDateTime joinDate

    @Column(name = 'LOCKEDIP', length = 30)
    private String lockedIp

    @Column(name = 'FAILED_LOGINS')
    private Long failedLogins

    @Column(name = 'LOCKED')
    private Byte locked
    
    @Column(name = 'ACTIVE_REALM_ID')
    private Long activeRealmId

    @JsonProperty
    @Column(name = 'EXPANSION')
    private Byte expansion

    @Column(name = 'MUTETIME')
    private Long muteTime

    @JsonProperty
    @Column(name = 'LOCALE')
    private String locale

    @Column(name = 'TOKEN')
    private String token

    @Column(name = 'OS')
    private String os

    @Column(name = 'PLATFORM')
    private String platform

    @Column(name = 'flags')
    private Integer flags

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
