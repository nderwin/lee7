/*
 * Copyright 2017 Nathan Erwin.
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

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.security.enterprise.AuthenticationException;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import net.minidev.json.JSONArray;

import static java.util.stream.Collectors.toSet;

/**
 *
 * @author nderwin
 */
@RequestScoped
public class AuthenticationMechanism implements HttpAuthenticationMechanism {

    private static final String BEARER = "Bearer ";
    
    @Inject
    RsaKeystore keystore;

    @Override
    public AuthenticationStatus validateRequest(final HttpServletRequest request, final HttpServletResponse response, final HttpMessageContext httpMessageContext) throws AuthenticationException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        
        if (null != authHeader && authHeader.startsWith(BEARER)) {
            String token = authHeader.substring(BEARER.length());
            
            try {
                SignedJWT jwt = SignedJWT.parse(token);
                JWTClaimsSet claims = jwt.getJWTClaimsSet();
                JSONArray roles = (JSONArray) claims.getClaim("roles");

                return httpMessageContext.notifyContainerAboutLogin(claims.getSubject(), roles.stream().map(r -> r.toString()).collect(toSet()));
            } catch (ParseException ex) {
                // TODO - probably should be something else...
                // TODO - logging
                return httpMessageContext.doNothing();
            }
        }
        
        return httpMessageContext.doNothing();
    }
    
}
