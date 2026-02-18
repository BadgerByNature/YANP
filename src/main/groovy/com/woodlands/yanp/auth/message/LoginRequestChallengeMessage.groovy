package com.woodlands.yanp.auth.message

import groovy.transform.ToString

@ToString(includeNames = true)
class LoginRequestChallengeMessage extends AuthMessage {
    byte error
    short size
    String gameName
    byte majorVersion
    byte minorVersion
    byte patchVersion
    short build
    String arch
    String os
    String locale
    int timezone
    int ip
    byte nameLength
    String accountName
}
