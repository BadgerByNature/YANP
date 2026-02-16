package com.woodlands.yanp.auth.db.repository

import com.woodlands.yanp.auth.db.entity.AccountBannedEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AccountBannedRepository extends JpaRepository<AccountBannedEntity, Long> {
    // TODO CMangos only pulls the banned_at and expires_at columns - is that actually more performant? Enough that we should we replicate that behavior?
    // It's a lot trickier to do cleanly in JPA https://www.w3docs.com/snippets/java/spring-jpa-selecting-specific-columns.html

    AccountBannedEntity findByIdAndActive(Long id, boolean active)
}
