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
package com.github.nderwin.lee7.authentication;

import com.github.nderwin.lee7.LogAspect;
import com.github.nderwin.lee7.authentication.boundary.AuthenticationHelper;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.interceptor.Interceptors;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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
import javax.security.auth.message.callback.PasswordValidationCallback;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static javax.security.auth.message.AuthStatus.SEND_FAILURE;
import static javax.security.auth.message.AuthStatus.SEND_SUCCESS;
import static javax.security.auth.message.AuthStatus.SUCCESS;

/**
 * A custom ServerAuthModule that looks for a header variable called
 * <code>X-Auth-Token</code> to use for validating access to the REST 
 * resource endpoints.
 * 
 * @author nderwin
 */
@Interceptors(LogAspect.class)
public class HttpServerAuthModule implements ServerAuthModule {

    private static final Logger LOG = Logger.getLogger(HttpServerAuthModule.class.getName());

    private CallbackHandler handler;

    private AuthenticationHelper authHelper;

    private final Class<?>[] supportedMessageTypes = new Class[]{
        HttpServletRequest.class,
        HttpServletResponse.class
    };

    public HttpServerAuthModule() {
        try {
            authHelper = (AuthenticationHelper) new InitialContext().lookup("java:module/AuthenticationHelper");
        } catch (NamingException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

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

        LOG.log(Level.CONFIG, "Authenticating {1} endpoint {0}", new Object[]{
            request.getRequestURI(),
            (mandatory ? "protected" : "anonymous")
        });

        if (mandatory) {
            String token = request.getHeader("X-Auth-Token");

            if (null == token) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return SEND_FAILURE;
            }
            
            UserInformation user = authHelper.validateToken(token);
            if (null != user) {
                callbacks = new Callback[]{
                    new CallerPrincipalCallback(clientSubject, user.getUsername()),
                    new PasswordValidationCallback(clientSubject, user.getUsername(), user.getPassword().toCharArray()),
                    new GroupPrincipalCallback(clientSubject, user.getRoles())
                };

                try {
                    handler.handle(callbacks);
                } catch (IOException | UnsupportedCallbackException e) {
                    throw (AuthException) new AuthException(e.getMessage()).initCause(e);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return SEND_FAILURE;
            }
        }

        return SUCCESS;
    }
}
