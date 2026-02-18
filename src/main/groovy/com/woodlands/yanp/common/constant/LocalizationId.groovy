package com.woodlands.yanp.common.constant

import java.util.stream.Stream

/**
 * Names for localization Ids.
 */
enum LocalizationId {
    ENGLISH(0),
    KOREAN(1),
    FRENCH(2),
    GERMAN(3),
    CHINESE(4),
    TAIWANESE(5),
    SPANISH_CASTILIAN(6),
    SPANISH_LATIN(7),
    RUSSIAN(8)

    int id

    LocalizationId(int id) {
        this.id = id
    }

    static LocalizationId fromId(int id) {
        Stream.of(values())
            .filter(l -> l.id == id)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid LocalizationId specified: ${id}"))
    }
}