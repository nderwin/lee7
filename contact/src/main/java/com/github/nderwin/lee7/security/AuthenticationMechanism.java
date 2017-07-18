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

import com.github.nderwin.lee7.security.entity.InvalidToken;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

    private static final Logger LOG = Logger.getLogger(AuthenticationMechanism.class.getName());
    
    private static final String BEARER = "Bearer ";
    
    @PersistenceContext
    EntityManager em;
    
    @Inject
    RsaKeystore keystore;

    @Override
    public AuthenticationStatus validateRequest(final HttpServletRequest request, final HttpServletResponse response, final HttpMessageContext httpMessageContext) throws AuthenticationException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        
        if (null != authHeader && authHeader.startsWith(BEARER)) {
            String token = authHeader.substring(BEARER.length());
            
            if (null == em.find(InvalidToken.class, token)) {
                try {
                    SignedJWT jwt = SignedJWT.parse(token);

                    JWSVerifier verifier = new RSASSAVerifier(keystore.getPublicKey());
                    if (jwt.verify(verifier)) {
                        JWTClaimsSet claims = jwt.getJWTClaimsSet();

                        Date now = new Date();
                        if (claims.getNotBeforeTime().after(now) || claims.getExpirationTime().before(now)) {
                            return httpMessageContext.doNothing();
                        }

                        JSONArray roles = (JSONArray) claims.getClaim("roles");

                        return httpMessageContext.notifyContainerAboutLogin(claims.getSubject(), roles.stream().map(r -> r.toString()).collect(toSet()));
                    } else {
                        return httpMessageContext.doNothing();
                    }
                } catch (ParseException | JOSEException ex) {
                    LOG.log(Level.WARNING, ex.getMessage(), ex);
                    return httpMessageContext.doNothing();
                }
            } else {
                return httpMessageContext.responseUnauthorized();
            }
        }
        
        return httpMessageContext.doNothing();
    }
    
}
