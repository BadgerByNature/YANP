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
package com.yanp.auth

import com.yanp.auth.constant.AuthStatus
import com.yanp.shared.db.entity.AccountEntity
import com.yanp.shared.srp.WowSrp6Server
import io.netty.util.AttributeKey

class AuthAttributeKey {
    /** AttributeKey for injecting the account into the channel*/
    public static final AttributeKey<AccountEntity> ACCOUNT = AttributeKey.newInstance('Account')
    /** AttributeKey for injecting build information into the channel for later steps to use */
    public static final AttributeKey<Short> BUILD = AttributeKey.newInstance('Build')
    /** AttributeKey for operating system the client passed in */
    public static final AttributeKey<String> OS = AttributeKey.newInstance('OS')
    /** AttributeKey for the value used when sending and validating our reconnect challenge */
    public static final AttributeKey<BigInteger> RECON_PROOF = AttributeKey.newInstance('Reconnect Proof')
    /** AttributeKey for injecting and retrieving our WowSrp6Server into/from our Channel for use in future processing on the same channel */
    public static final AttributeKey<WowSrp6Server> SRP_ATTRIBUTE = AttributeKey.newInstance('SRP')
    /** AttributeKey for injecting current Auth Status into the channel */
    public static final AttributeKey<AuthStatus> STATUS = AttributeKey.newInstance('Auth Status')
}
