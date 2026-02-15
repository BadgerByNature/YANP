package com.woodlands.yanp.auth.message

import groovy.transform.ToString

@ToString(includeNames = true)
class LoginRequestMessage extends AuthMessage {
    byte error
    short size
    byte[] gameName
    byte majorVersion
    byte minorVersion
    byte patchVersion
    short build
    byte[] arch
    byte[] os
    byte[] locale
    int timezone
    int ip
    byte nameLength
    byte[] accountName
}
