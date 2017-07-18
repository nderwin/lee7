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
package com.github.nderwin.lee7.security.boundary;

import com.github.nderwin.lee7.security.RsaKeystore;
import com.github.nderwin.lee7.security.entity.InvalidToken;
import com.github.nderwin.lee7.security.entity.User;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.mindrot.BCrypt;

/**
 *
 * @author nderwin
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Path("/authentication")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"USER"})
public class AuthenticationResource {

    private static final Logger LOG = Logger.getLogger(AuthenticationResource.class.getName());

    private static final String BEARER = "Bearer ";

    @PersistenceContext
    EntityManager em;
    
    @Inject
    RsaKeystore keystore;

    @POST
    @Path("/login")
    @PermitAll
    public Response login(final User body) {
        JsonObjectBuilder job = Json.createObjectBuilder();

        try {
            User user = em.createNamedQuery("User.findByUsername", User.class)
                    .setParameter("username", body.getUsername())
                    .getSingleResult();

            if (BCrypt.checkpw(body.getPassword(), user.getPassword())) {
                JWSSigner signer = new RSASSASigner(keystore.getPrivateKey());
                
                Date now = new Date();

                JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                        .subject(user.getUsername())
                        .issuer("lee7")
                        .expirationTime(new Date(now.getTime() + (60 * 1000)))
                        .issueTime(now)
                        .notBeforeTime(now)
                        .claim("roles", user.getRoles())
                        .build();

                SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);

                try {
                    jwt.sign(signer);
                } catch (JOSEException ex) {
                    throw new RuntimeException(ex);
                }
                
                job.add("token", jwt.serialize());

                return Response.ok(job.build()).build();
            } else {
                LOG.log(Level.INFO, "Incorrect password entered for user {0}", body.getPassword());
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (NoResultException | NonUniqueResultException ex) {
            LOG.log(Level.INFO, "{0}; username: {1}", new Object[]{
                ex.getMessage(),
                body.getUsername()
            });
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

    }

    @GET
    @PUT
    @POST
    @Path("/logout")
    public Response logout(@Context HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = authHeader.substring(BEARER.length());

        try {
            SignedJWT jwt = SignedJWT.parse(token);
            
            InvalidToken it = new InvalidToken(token, jwt.getJWTClaimsSet().getExpirationTime());
            em.persist(it);
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            // TODO - better status?
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        
        return Response.ok().build();
    }
}
