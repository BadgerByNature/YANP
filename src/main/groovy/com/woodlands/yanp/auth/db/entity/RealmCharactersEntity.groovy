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
package com.woodlands.yanp.auth.db.entity

import groovy.transform.EqualsAndHashCode
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table

@Entity
@IdClass(RealmCharactersIdClass)
@Table(name = "realmcharacters", catalog = 'tbcrealmd')
class RealmCharactersEntity {

    @Id
    @Column(name = "realmid")
    Integer realmId

    @Id
    @Column(name = 'acctid')
    Integer accountId

    @Column(name = 'numchars')
    Integer count
}

@EqualsAndHashCode
class RealmCharactersIdClass implements Serializable {
    Integer realmId
    Integer accountId
}
