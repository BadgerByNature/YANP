package com.woodlands.yanp.auth.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "realmlist", schema = 'tbcrealmd', catalog = 'tbcrealmd')
class RealmEntity {

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id

    /** The realm name. Max 32 characters */
    @Column(name = "NAME", length = 32, nullable = false)
    private String name

    /** The IP address. */
    @Column(name = "ADDRESS", length = 32, nullable = false)
    private String address

    /** The port to connect on. */
    @Column(name = "PORT")
    private Integer port

    /** The icon. */
    @Column(name = "ICON")
    private Integer icon

    /** The realmflags. */
    @Column(name = "REALMFLAGS")
    private Integer realmFlags

    /** The timezone. */
    @Column(name = "TIMEZONE")
    private Integer timezone

    /** The allowed security level. */
    @Column(name = "ALLOWEDSECURITYLEVEL")
    private Integer allowedSecurityLevel

    /** The population. */
    @Column(name = "POPULATION")
    private Float population

    /** The realmbuilds. */
    @Column(name = "REALMBUILDS", length = 64, nullable = false)
    private String realmBuilds

    Integer getId() {
        return this.id
    }

    void setId(final Integer id) {
        this.id = id
    }

    String getName() {
        return this.name
    }

    void setName(final String name) {
        this.name = name
    }

    String getAddress() {
        return this.address
    }

    void setAddress(final String address) {
        this.address = address
    }

    Integer getPort() {
        return this.port
    }

    void setPort(final Integer port) {
        this.port = port
    }

    Integer getIcon() {
        return this.icon
    }

    void setIcon(final Integer icon) {
        this.icon = icon
    }

    Integer getRealmFlags() {
        return this.realmFlags
    }

    void setRealmFlags(final Integer realmFlags) {
        this.realmFlags = realmFlags
    }

    Integer getTimezone() {
        return this.timezone
    }

    void setTimezone(final Integer timezone) {
        this.timezone = timezone
    }

    Integer getAllowedSecurityLevel() {
        return this.allowedSecurityLevel
    }

    void setAllowedSecurityLevel(final Integer allowedSecurityLevel) {
        this.allowedSecurityLevel = allowedSecurityLevel
    }

    Float getPopulation() {
        return this.population
    }

    void setPopulation(final Float population) {
        this.population = population
    }

    String getRealmBuilds() {
        return this.realmBuilds
    }

    void setRealmBuilds(final String realmBuilds) {
        this.realmBuilds = realmBuilds
    }

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
