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

import java.util.HashMap;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.ClientAuthConfig;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.module.ServerAuthModule;

/**
 * Scaffolding class to allow for a custom ServerAuthModule.
 * 
 * @author nderwin
 * @see ServerAuthModule
 * @see HttpServerAuthModule
 */
public class HttpAuthConfigProvider implements AuthConfigProvider {

    private static final String CALLBACK_HANDLER_PROPERTY_NAME = "authconfigprovider.client.callbackhandler";

    private final Map<String, String> providerProperties = new HashMap<>();

    private ServerAuthModule serverAuthModule;

    public HttpAuthConfigProvider(final ServerAuthModule serverAuthModule) {
        this.serverAuthModule = serverAuthModule;
    }

    public HttpAuthConfigProvider(final Map<String, String> providerProperties, final AuthConfigFactory factory) {
        this.providerProperties.putAll(providerProperties);

        if (null != factory) {
            factory.registerConfigProvider(this, null, null, "Auto registration");
        }
    }

    @Override
    public ClientAuthConfig getClientAuthConfig(final String layer, final String appContext, final CallbackHandler handler) throws AuthException, SecurityException {
        return null;
    }

    @Override
    public ServerAuthConfig getServerAuthConfig(final String layer, final String appContext, final CallbackHandler handler) throws AuthException, SecurityException {
        return new HttpServerAuthConfig(layer, appContext, (handler == null ? createDefaultCallbackHandler() : handler), providerProperties, serverAuthModule);
    }

    @Override
    public void refresh() {
    }

    private CallbackHandler createDefaultCallbackHandler() throws AuthException {
        String callBackClassName = System.getProperty(CALLBACK_HANDLER_PROPERTY_NAME);

        if (null == callBackClassName) {
            throw new AuthException("No default handler set via system property: " + CALLBACK_HANDLER_PROPERTY_NAME);
        }

        try {
            return (CallbackHandler) Thread.currentThread().getContextClassLoader().loadClass(callBackClassName).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new AuthException(e.getMessage());
        }
    }
}
