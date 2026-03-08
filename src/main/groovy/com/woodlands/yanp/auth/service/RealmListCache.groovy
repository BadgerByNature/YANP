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

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.woodlands.yanp.auth.db.entity.RealmEntity
import com.woodlands.yanp.auth.db.repository.RealmRepository
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import java.util.concurrent.TimeUnit

/**
 * Cache for the realm list so that it doesn't have to be loaded on every single realmList request.
 * Configurable maximum update interval, but will not update when no one is requesting the information.
 */
@Slf4j
@Component
class RealmListCache {

    public final static String REALM_LIST = "realmlist"

    final LoadingCache<String, List<RealmEntity>> cache

    RealmListCache(RealmRepository realmRepository, @Value('${auth.realmsUpdateIntervalSeconds}') int updateIntervalSeconds) {
        cache = Caffeine.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(updateIntervalSeconds, TimeUnit.SECONDS)
                .build { list -> realmRepository.findAll() }
    }
}
