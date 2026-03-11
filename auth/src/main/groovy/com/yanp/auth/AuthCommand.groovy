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

/**
 * Enum representing commands sent from the client to the server as part of the Auth workflow
 */
enum AuthCommand {

    CMD_AUTH_REQUEST_LOGIN_CHALLENGE(0x00, AuthStatus.CHALLENGE), // Request a Login Challenge from the Server
    CMD_AUTH_LOGIN_PROOF(0x01, AuthStatus.LOGIN_PROOF),
    CMD_AUTH_RECONNECT_CHALLENGE(0x02, AuthStatus.CHALLENGE),
    CMD_AUTH_RECONNECT_PROOF(0x03, AuthStatus.RECON_PROOF),
    CMD_REALM_LIST(0x10, AuthStatus.AUTHED)

    AuthStatus expectedAuthStatus
    int code

    AuthCommand(int code, AuthStatus expectedAuthStatus) {
        this.expectedAuthStatus = expectedAuthStatus
        this.code = code
    }

    static AuthCommand fromCode(byte code) {
        for (AuthCommand command : values()) {
            if (command.code == code) {
                return command
            }
        }
        throw new IllegalArgumentException("Invalid AuthCommand: ${Integer.toHexString(code)}")
    }
}