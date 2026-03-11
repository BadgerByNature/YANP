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
package com.yanp.auth.db.entity

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.EqualsAndHashCode
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@EqualsAndHashCode
@Entity
@Table(name = "realmlist", catalog = 'tbcrealmd')
class RealmEntity {

    @JsonProperty
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id

    /** The realm name. Max 32 characters */
    @JsonProperty
    @Column(name = "NAME", length = 32, nullable = false)
    String name

    /** The IP address. */
    @Column(name = "ADDRESS", length = 32, nullable = false)
    String address

    /** The port to connect on. */
    @Column(name = "PORT")
    Integer port

    /** The icon. */
    @Column(name = "ICON")
    Integer icon

    /** The realmflags. */
    @JsonProperty
    @Column(name = "REALMFLAGS")
    Integer realmFlags

    /** The timezone. */
    @JsonProperty
    @Column(name = "TIMEZONE")
    Integer timezone

    /** The allowed security level. */
    @Column(name = "ALLOWEDSECURITYLEVEL")
    Integer allowedSecurityLevel

    /** The population. */
    @JsonProperty
    @Column(name = "POPULATION")
    Float population

    /** The realmbuilds. */
    @JsonProperty
    @Column(name = "REALMBUILDS", length = 64, nullable = false)
    String realmBuilds
}
