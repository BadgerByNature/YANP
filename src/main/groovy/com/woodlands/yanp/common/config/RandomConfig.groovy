//file:noinspection GrMethodMayBeStatic
package com.woodlands.yanp.common.config

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