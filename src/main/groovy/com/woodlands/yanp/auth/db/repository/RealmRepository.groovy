package com.woodlands.yanp.auth.db.repository

import com.woodlands.yanp.auth.db.entity.RealmEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RealmRepository extends JpaRepository<RealmEntity, Integer> {

}