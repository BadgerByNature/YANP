package com.woodlands.yanp.auth.repository

import com.woodlands.yanp.auth.entity.RealmEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RealmRepository extends JpaRepository<RealmEntity, Integer> {

}