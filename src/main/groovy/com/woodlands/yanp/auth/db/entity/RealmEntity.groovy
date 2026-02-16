package com.woodlands.yanp.auth.db.entity

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "realmlist", catalog = 'tbcrealmd')
class RealmEntity {

    @JsonProperty
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @Override
    int hashCode() {
        return Objects.hash(this.id, this.name)
    }

    @Override
    boolean equals(final Object obj) {

        if (this == obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (getClass() != obj.getClass()) {
            return false
        }
        final RealmEntity other = (RealmEntity) obj
        return Objects.equals(this.id, other.id) && Objects.equals(this.name, other.name)
    }

}
