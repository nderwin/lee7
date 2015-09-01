package com.github.nderwin.lee7.authentication;

import java.util.Collections;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.module.ServerAuthModule;

public class HttpServerAuthContext implements ServerAuthContext {

    private final ServerAuthModule serverAuthModule;
    
    public HttpServerAuthContext(final CallbackHandler handler, final ServerAuthModule serverAuthModule) throws AuthException {
        this.serverAuthModule = serverAuthModule;
        serverAuthModule.initialize(null, null, handler, Collections.<String, String> emptyMap());
    }

    @Override
    public void cleanSubject(final MessageInfo messageInfo, final Subject subject) throws AuthException {
        serverAuthModule.cleanSubject(messageInfo, subject);
    }

    @Override
    public AuthStatus secureResponse(final MessageInfo messageInfo, final Subject serviceSubject) throws AuthException {
        return serverAuthModule.secureResponse(messageInfo, serviceSubject);
    }

    @Override
    public AuthStatus validateRequest(final MessageInfo messageInfo, final Subject clientSubject, final Subject serviceSubject) throws AuthException {
        return serverAuthModule.validateRequest(messageInfo, clientSubject, serviceSubject);
    }
    
}
