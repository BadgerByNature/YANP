package com.woodlands.yanp.web.controller

import com.woodlands.yanp.auth.entity.AccountEntity
import com.woodlands.yanp.auth.entity.RealmEntity
import com.woodlands.yanp.auth.repository.AccountRepository
import com.woodlands.yanp.auth.repository.RealmRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping('api/v1/')
class TestController {

    private final AccountRepository accountRepository
    private final RealmRepository realmRepository

    TestController(
        AccountRepository accountRepository,
        RealmRepository realmRepository
    ) {
        this.accountRepository = accountRepository
        this.realmRepository = realmRepository
    }

    @GetMapping('realms')
    List<RealmEntity> getAllRealms() {
        return realmRepository.findAll()
    }

    @GetMapping('account/{id}')
    AccountEntity getAccountById(long id) {
        accountRepository.findById(id).orElseGet { null }
    }
}
