package com.woodlands.yanp.auth.repository

import com.woodlands.yanp.auth.entity.AccountEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository extends JpaRepository<AccountEntity, Long> {

}