package com.woodlands.yanp.auth.entity


import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

import java.time.LocalDateTime

@Entity
@Table(name = "account", schema = 'tbcrealmd', catalog = 'tbcrealmd')
class AccountEntity {

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id

    /** The user name. */
    @Column(name = "USERNAME", length = 32, unique = true)
    private String username

    /** Access level of account 0 = regular user, > 0 = GM. */
    @Column(name = "GMLEVEL")
    private Byte gmLevel

    /** The session key. */
    @Column(name = "SESSIONKEY")
    private String sessionKey

    /** The v. */
    @Column(name = "V", nullable = false)
    private String v

    /** The s. */
    @Column(name = "S", nullable = false)
    private String s

    @Column(name = "EMAIL")
    private String email

    @Column(name = "JOINDATE")
    private LocalDateTime joinDate

    @Column(name = "LOCKEDIP", length = 30)
    private String lockedIp

    @Column(name = "FAILED_LOGINS")
    private Long failedLogins

    /** Account activated. */
    @Column(name = "LOCKED")
    private Byte locked

    @Column(name = "LAST_LOGIN")
    private Calendar lastLogin

    @Column(name = "ACTIVE_REALM_ID")
    private Long activeRealmId

    @Column(name = "EXPANSION")
    private Byte expansion

    @Column(name = "MUTETIME")
    private Long muteTime

    @Column(name = "LOCALE")
    private Byte locale

    @Column(name = "LAST_SERVER")
    private Byte lastServer

    // TODO token, os, platform, flags in CMangos

    Long getId() {
        return this.id
    }

    void setId(final Long id) {
        this.id = id
    }

    String getUsername() {
        return this.username
    }

    void setUsername(final String username) {
        this.username = username
    }

    Byte getGMLevel() {
        return this.gmLevel
    }

    void setGMLevel(final Byte gmlevel) {
        this.gmLevel = gmlevel
    }

    String getSessionKey() {
        return this.sessionKey
    }

    void setSessionKey(final String sessionKey) {
        this.sessionKey = sessionKey
    }

    String getV() {
        return this.v
    }

    void setV(final String v) {
        this.v = v
    }

    String getS() {
        return this.s
    }

    void setS(final String s) {
        this.s = s
    }

    String getEmail() {
        return this.email
    }

    void setEmail(final String email) {
        this.email = email
    }

    LocalDateTime getJoinDate() {
        return this.joinDate
    }

    void setJoinDate(final LocalDateTime joinDate) {
        this.joinDate = joinDate
    }

    String getLastIp() {
        return this.lockedIp
    }

    void setLastIp(final String lastIp) {
        this.lockedIp = lastIp
    }

    Long getFailedLogins() {
        return this.failedLogins
    }

    void setFailedLogins(final Long failedLogins) {
        this.failedLogins = failedLogins
    }

    Byte getLocked() {
        return this.locked
    }

    void setLocked(final Byte locked) {
        this.locked = locked
    }

    Calendar getLastLogin() {
        return this.lastLogin
    }

    void setLastLogin(final Calendar lastLogin) {
        this.lastLogin = lastLogin
    }

    Long getActiveRealmId() {
        return this.activeRealmId
    }

    void setActiveRealmId(final Long activeRealmId) {
        this.activeRealmId = activeRealmId
    }

    Byte getExpansion() {
        return this.expansion
    }

    void setExpansion(final Byte expansion) {
        this.expansion = expansion
    }

    Long getMuteTime() {
        return this.muteTime
    }

    void setMuteTime(final Long muteTime) {
        this.muteTime = muteTime
    }

    Byte getLocale() {
        return this.locale
    }

    void setLocale(final Byte locale) {
        this.locale = locale
    }

    Byte getLastServer() {

        return this.lastServer
    }

    void setLastServer(final Byte lastServer) {
        this.lastServer = lastServer
    }

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
