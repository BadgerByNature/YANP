package com.woodlands.yanp.auth.db.repository

import com.woodlands.yanp.auth.db.entity.IpBannedEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
// TODO This should actually be a composite primary key of `ip` and `banned_at` for some reason (in CMangos) - for now let's just pretend ip is the single primary key column
interface IpBannedRepository extends JpaRepository<IpBannedEntity, String> {

    @Query('SELECT ban FROM IpBannedEntity ban WHERE (ban.expiresAt = ban.bannedAt OR ban.expiresAt > :now) AND ban.ipAddress = :ip')
    IpBannedEntity findBannedIp(@Param('ip') String ip, @Param('now') Long now)
}