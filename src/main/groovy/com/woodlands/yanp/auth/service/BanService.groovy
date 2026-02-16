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
        def banRecord = accountBannedRepository.findByIdAndActive(accountId, true)
        if (!banRecord) return BanStatus.NONE
        if (banRecord.bannedAt == banRecord.expiresAt) return BanStatus.PERMANENT
        return BanStatus.TEMPORARY
    }
}
