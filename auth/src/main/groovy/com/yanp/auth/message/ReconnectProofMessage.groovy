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
package com.yanp.auth.message

class ReconnectProofMessage extends AuthMessage {

    byte[] R1 // A client proof for both srp validation and client hash validation
    byte[] R2 // The expected hash value of the proof against the username, session key, and server random
    byte[] R3 // The expected hash value of the proof against the server's hash for the client version
    byte numberOfKeys // Unused
}
