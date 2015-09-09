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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.module.ServerAuthModule;

/**
 * Scaffolding class to allow for a custom ServerAuthModule.
 * 
 * @author nderwin
 * @see ServerAuthModule
 * @see HttpServerAuthModule
 */
public class HttpServerAuthConfig implements ServerAuthConfig {

    private final String layer;

    private final String appContext;

    private final CallbackHandler handler;

    private final Map<String, String> providerProperties = new HashMap<>();

    private final ServerAuthModule serverAuthModule;

    public HttpServerAuthConfig(final String layer, final String appContext, final CallbackHandler handler, final Map<String, String> providerProperties, final ServerAuthModule serverAuthModule) {
        this.layer = layer;
        this.appContext = appContext;
        this.handler = handler;
        this.providerProperties.putAll(providerProperties);
        this.serverAuthModule = serverAuthModule;
    }

    @Override
    public ServerAuthContext getAuthContext(final String authContextID, final Subject serviceSubject, final Map properties) throws AuthException {
        return new HttpServerAuthContext(handler, serverAuthModule);
    }

    @Override
    public String getAppContext() {
        return appContext;
    }

    @Override
    public String getAuthContextID(final MessageInfo messageInfo) {
        return appContext;
    }

    @Override
    public String getMessageLayer() {
        return layer;
    }

    @Override
    public boolean isProtected() {
        return false;
    }

    @Override
    public void refresh() {
    }

    public Map<String, String> getProviderProperties() {
        return Collections.unmodifiableMap(providerProperties);
    }
}
