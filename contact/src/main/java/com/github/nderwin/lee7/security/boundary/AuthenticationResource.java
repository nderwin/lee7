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
package com.github.nderwin.lee7.security.boundary;

import com.github.nderwin.lee7.LogAspect;
import com.github.nderwin.lee7.security.entity.User;
import com.github.nderwin.lee7.security.RsaUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

/**
 * A RESTful web service that manages the authentication tokens for an
 * authorized user.
 *
 * @author nderwin
 * @see User
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Interceptors(LogAspect.class)
public class AuthenticationResource {

    private static final Logger LOG = Logger.getLogger(AuthenticationResource.class.getName());

    @PersistenceContext
    EntityManager em;
    
    @Inject
    RsaUtil rsa;

    @POST
    @Path("/login")
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public Response login(@Context final HttpServletRequest request, final LoginPayload payload) {
        Response resp;

        try {
            // validate username and password (insensitive search for username)
            User user = em.createNamedQuery("findByUsername", User.class)
                    .setParameter("username", payload.getUsername())
                    .getSingleResult();

            if (user.getPassword().equals(payload.getPassword())) {
                try {
                    JwtClaims claims = new JwtClaims();
                    claims.setIssuer(rsa.getIssuer());
                    claims.setExpirationTimeMinutesInTheFuture(request.getSession().getMaxInactiveInterval() / 60);
                    claims.setGeneratedJwtId();
                    claims.setIssuedAtToNow();
                    claims.setNotBeforeMinutesInThePast(2);
                    claims.setSubject(payload.getUsername());
                    List<String> roles = new ArrayList<>(user.getRoles().size());
                    user.getRoles().stream().forEach((r) -> {
                        roles.add(r.getName());
                    });
                    claims.setStringListClaim("groups", roles);

                    JsonWebSignature jws = new JsonWebSignature();
                    jws.setPayload(claims.toJson());
                    jws.setKey(rsa.getRsaJsonWebKey().getPrivateKey());
                    jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
                    
                    resp = Response.ok(jws.getCompactSerialization()).build();
                } catch (JoseException ex) {
                    LOG.log(Level.SEVERE, ex.getMessage());
                    resp = Response.serverError().entity(ex.getMessage()).build();
                }
            } else {
                LOG.log(Level.INFO, "Password mismatch for user {0}", user.getUsername());
                resp = Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (NoResultException | NonUniqueResultException ex) {
            LOG.log(Level.INFO, "{0} for {1}", new Object[]{ex.getMessage(), payload.getUsername()});
            resp = Response.status(Response.Status.UNAUTHORIZED).build();
        }

        System.out.println("***** login response: " + resp.getStatus() + " : " + resp.getEntity());
        return resp;
    }

    @PUT
    @Path("/logout")
    @RolesAllowed({"authenticated-user"})
    public Response logout(@Context final HttpServletRequest request) throws ServletException, JoseException {
        JwtConsumer consumer = new JwtConsumerBuilder()
                .setRequireExpirationTime()
                .setAllowedClockSkewInSeconds(30)
                .setRequireSubject()
                .setExpectedIssuer("Issuer")
                .setExpectedAudience("Audience")
                .setVerificationKey(rsa.getRsaJsonWebKey().getKey())
                .build();
        String token = request.getHeader("Authorization");
        
        if (null != token) {
            try {
                // TODO - do we need to do anything with the claims at this point?
                // TODO - is there some way we can invalidate this token so it is not used again?
                JwtClaims claims = consumer.processToClaims(token);
            } catch (InvalidJwtException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
        }

        request.logout();

        return Response.ok().build();
    }

}
