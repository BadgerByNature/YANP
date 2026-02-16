package com.woodlands.yanp.auth.db.repository

import com.woodlands.yanp.auth.db.entity.AccountEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    AccountEntity findByUsername(String username)
}