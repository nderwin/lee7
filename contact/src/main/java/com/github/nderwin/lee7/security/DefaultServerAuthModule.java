/*
 * Copyright 2015 Nathan Erwin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.nderwin.lee7.security;

import com.github.nderwin.lee7.LogAspect;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.spi.CDI;
import javax.interceptor.Interceptors;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jsr375.identitystore.CredentialValidationResult;
import jsr375.identitystore.IdentityStore;
import jsr375.identitystore.credential.TokenCredential;

import static javax.security.auth.message.AuthStatus.SEND_SUCCESS;
import static javax.security.auth.message.AuthStatus.SUCCESS;
import static jsr375.identitystore.CredentialValidationResult.Status.VALID;

/**
 * 
 * @author nderwin
 */
@Interceptors(LogAspect.class)
public class DefaultServerAuthModule implements ServerAuthModule {

    private static final Logger LOG = Logger.getLogger(DefaultServerAuthModule.class.getName());

    private CallbackHandler handler;

    private final Class<?>[] supportedMessageTypes = new Class[]{
        HttpServletRequest.class,
        HttpServletResponse.class
    };

    @Override
    public Class[] getSupportedMessageTypes() {
        return supportedMessageTypes;
    }

    @Override
    public void initialize(final MessagePolicy requestPolicy, final MessagePolicy responsePolicy, final CallbackHandler handler, final Map options) throws AuthException {
        this.handler = handler;
    }

    @Override
    public void cleanSubject(final MessageInfo messageInfo, final Subject subject) throws AuthException {
    }

    @Override
    public AuthStatus secureResponse(final MessageInfo messageInfo, final Subject serviceSubject) throws AuthException {
        return SEND_SUCCESS;
    }

    @Override
    public AuthStatus validateRequest(final MessageInfo messageInfo, final Subject clientSubject, final Subject serviceSubject) throws AuthException {
        HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
        HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();

        Callback[] callbacks;

        boolean mandatory = Boolean.parseBoolean(
                messageInfo.getMap().get("javax.security.auth.message.MessagePolicy.isMandatory").toString()
        );

        LOG.log(Level.CONFIG, "Authenticating {0} endpoint {1}", new Object[]{
            (mandatory ? "PROTECTED" : "ANONYMOUS"),
            request.getRequestURI()
        });

        if (mandatory) {
            String token = request.getHeader("Authorization");
            if (null != token) {
                IdentityStore identityStore = CDI.current().select(IdentityStore.class).get();
                
                CredentialValidationResult result = identityStore.validate(new TokenCredential(false, "", token));
                
                if (result.getStatus() == VALID) {
                    callbacks = new Callback[] {
                        new CallerPrincipalCallback(clientSubject, result.getCallerName()),
                        new GroupPrincipalCallback(clientSubject, result.getCallerGroups().toArray(new String[0]))
                    };
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    throw new AuthException("Unauthorized access");
                }
            } else {
                callbacks = new Callback[] {
                    new CallerPrincipalCallback(clientSubject, (Principal) null)
                };
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }

            try {
                handler.handle(callbacks);
            } catch (IOException | UnsupportedCallbackException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                throw (AuthException) new AuthException().initCause(e);
            }
        }

        return SUCCESS;
    }
}
