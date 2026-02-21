package com.woodlands.yanp.auth.message

import groovy.transform.ToString

@ToString(includeNames = true)
class LoginProofMessage extends AuthMessage {
    BigInteger A
    BigInteger M1
    byte[] crcHash
    byte numberOfKeys // Unused?
    byte securityFlags
}
