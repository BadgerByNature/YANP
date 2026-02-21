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
package com.woodlands.yanp.auth.service

import com.woodlands.yanp.auth.constant.BanStatus
import com.woodlands.yanp.auth.db.repository.AccountBannedRepository
import com.woodlands.yanp.auth.db.repository.IpBannedRepository
import org.springframework.stereotype.Service

import java.time.Instant

@Service
class BanService {

    final AccountBannedRepository accountBannedRepository
    final IpBannedRepository ipBanRepository

    BanService(AccountBannedRepository accountBannedRepository, IpBannedRepository ipBanRepository) {
        this.accountBannedRepository = accountBannedRepository
        this.ipBanRepository = ipBanRepository
    }

    boolean isIpBanned(String ip) {
        ipBanRepository.findBannedIp(ip, Instant.now().epochSecond) != null
    }

    BanStatus getAccountBanStatus(Long accountId) {
        def banRecord = accountBannedRepository.findByIdAndIsActive(accountId, true)
        if (!banRecord) return BanStatus.NONE
        if (banRecord.bannedAt == banRecord.expiresAt) return BanStatus.PERMANENT
        return BanStatus.TEMPORARY
    }
}
