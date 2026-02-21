package com.woodlands.yanp.auth

/**
 * Enum representing commands sent from the client to the server as part of the Auth workflow
 */
enum AuthCommand {

    CMD_AUTH_REQUEST_LOGIN_CHALLENGE(0x00), // Request a Login Challenge from the Server
    CMD_AUTH_LOGIN_PROOF(0x01),
    CMD_AUTH_RECONNECT_CHALLENGE(0x02),
    CMD_AUTH_RECONNECT_PROOF(0x03),
    CMD_REALM_LIST(0x10),
    CMD_XFER_INITIATE(0x30),
    CMD_XFER_DATA(0x31)

    // According to vMangos and CMangos the codes below are not in the supported clients
//    CMD_XFER_ACCEPT(0x32),
//    CMD_XFER_RESUME(0x33),
//    CMD_XFER_CANCEL(0x34)

    int code

    AuthCommand(int code) {
        this.code = code;
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