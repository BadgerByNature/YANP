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
package com.woodlands.yanp.common.service

import com.woodlands.yanp.common.db.entity.AccountEntity
import com.woodlands.yanp.common.db.entity.AccountLoginsEntity
import com.woodlands.yanp.common.db.entity.LoginSource
import com.woodlands.yanp.common.db.repository.AccountLoginsRepository
import com.woodlands.yanp.common.db.repository.AccountRepository
import org.springframework.stereotype.Service

import java.time.LocalDateTime

@Service
class AccountService {

    final AccountRepository accountRepository
    final AccountLoginsRepository loginsRepository

    AccountService(AccountRepository accountRepository, AccountLoginsRepository loginsRepository) {
        this.accountRepository = accountRepository
        this.loginsRepository = loginsRepository
    }

    AccountEntity getAccount(String accountName) {
        accountRepository.findByUsername(accountName)
    }

    AccountEntity save(AccountEntity entity) {
        accountRepository.save(entity)
    }

    void saveLogin(AccountEntity account, String ip, LoginSource source) {
        def login = new AccountLoginsEntity(
                accountId: account.id,
                ip: ip,
                loginTime: LocalDateTime.now(),
                loginSource: source
        )
        loginsRepository.save(login)
    }
}
