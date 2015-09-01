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
