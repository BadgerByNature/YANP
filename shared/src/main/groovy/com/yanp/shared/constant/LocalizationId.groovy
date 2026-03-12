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