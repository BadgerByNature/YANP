package com.woodlands.yanp.common.constants

enum LocalizationId {
    ENGLISH(0),
    KOREAN(1),
    FRENCH(2),
    GERMAN(3),
    CHINESE(4),
    TAIWANESE(5),
    SPANISH(6),
    MEXICO(7),
    RUSSIAN(8)

    int id

    LocalizationId(int id) {
        this.id = id
    }
}