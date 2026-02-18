package com.woodlands.yanp.auth.db.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = 'ip_banned', catalog = 'tbcrealmd')
class IpBannedEntity {

    @Id
    @Column(name = 'ip', nullable = false)
    String ipAddress

    @Column(name = 'banned_at', nullable = false)
    Long bannedAt

    @Column(name = 'expires_at', nullable = false)
    Long expiresAt

    @Column(name = 'banned_by')
    String bannedBy

    @Column(name = 'reason')
    String reason
}
