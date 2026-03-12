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
package com.yanp.shared.constant

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