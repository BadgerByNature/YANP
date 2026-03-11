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
//file:noinspection GrMethodMayBeStatic
package com.yanp.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import java.security.SecureRandom

/**
 * Configuration class to create beans of our Random number generators. These
 * classes are intended to be shared throughout the app and are entirely threadsafe.
 */
@Configuration
class RandomConfig {

    @Bean
    SecureRandom secureRandom() {
        new SecureRandom()
    }

    @Bean
    Random random() {
        new Random()
    }
}