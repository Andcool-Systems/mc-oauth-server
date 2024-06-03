package com.andcool.session;


public class Session {
    public int protocolVersion = 0;
    public String nickname;
    public int nextState = 0;
    public int loginPhase = 0;  // 0 - waiting for handshake
                                // 1 - waiting for login request
                                // 2 - waiting for encryption response

    public Session() {
    }
}