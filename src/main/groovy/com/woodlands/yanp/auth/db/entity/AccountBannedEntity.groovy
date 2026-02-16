package com.woodlands.yanp.auth.db.entity

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

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
