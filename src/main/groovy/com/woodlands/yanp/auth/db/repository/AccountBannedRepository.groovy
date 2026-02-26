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
package com.woodlands.yanp.auth.db.repository

import com.woodlands.yanp.auth.db.entity.AccountBannedEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AccountBannedRepository extends JpaRepository<AccountBannedEntity, Long> {
    // TODO CMangos only pulls the banned_at and expires_at columns - is that actually more performant? Enough that we should we replicate that behavior?
    // It's a lot trickier to do cleanly in JPA https://www.w3docs.com/snippets/java/spring-jpa-selecting-specific-columns.html

    AccountBannedEntity findByIdAndIsActive(Integer id, boolean active)
}
