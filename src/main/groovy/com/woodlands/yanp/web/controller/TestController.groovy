/*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright (c) 2026 YANP: You Are Not Prepared
* See CONTRIBUTORS.md for further Copyright information
*/
package com.woodlands.yanp.web.controller

import com.woodlands.yanp.auth.db.entity.AccountEntity
import com.woodlands.yanp.auth.db.entity.RealmEntity
import com.woodlands.yanp.auth.db.repository.AccountRepository
import com.woodlands.yanp.auth.db.repository.RealmRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
    AccountEntity getAccountById(@PathVariable('id') long id) {
        accountRepository.findById(id).orElseGet { null }
    }
}
