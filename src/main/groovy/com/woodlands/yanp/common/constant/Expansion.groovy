package com.woodlands.yanp.common.constant

import java.util.stream.Stream

enum Expansion {
    VANILlA(0),
    TBC(1),
    WOTLK(2)

    int value

    Expansion(int value) {
        this.value = value
    }

    static Expansion fromValue(int value) {
        Stream.of(values())
            .filter(e -> e.value == value)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid Expansion specified: ${value}"))
    }
}