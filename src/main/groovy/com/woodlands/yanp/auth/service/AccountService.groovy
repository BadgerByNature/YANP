package com.woodlands.yanp.auth.service

import com.woodlands.yanp.auth.db.entity.AccountEntity
import com.woodlands.yanp.auth.db.repository.AccountRepository
import org.springframework.stereotype.Service

@Service
class AccountService {

    final AccountRepository repository

    AccountService(AccountRepository repository) {
        this.repository = repository
    }

    AccountEntity getAccount(String accountName) {
        repository.findByUsername(accountName)
    }
}
