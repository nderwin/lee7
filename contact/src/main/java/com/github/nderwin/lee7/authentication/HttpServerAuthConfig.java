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
