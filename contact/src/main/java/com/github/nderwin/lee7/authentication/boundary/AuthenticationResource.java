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
package com.github.nderwin.lee7.authentication.boundary;

import com.github.nderwin.lee7.LogAspect;
import com.github.nderwin.lee7.authentication.entity.AuthenticationToken;
import com.github.nderwin.lee7.authentication.entity.User;
import java.util.UUID;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
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

/**
 *
 * @author nderwin
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Interceptors(LogAspect.class)
public class AuthenticationResource {
    
    @PersistenceContext
    EntityManager em;
    
    @POST
    @Path("/login")
    @PermitAll
    public Response login(@Context final HttpServletRequest request, final LoginPayload payload) {
        Response resp;
        
        try {
            // validate username and password (insensitive search for username)
            User user = em.createNamedQuery("findByUsername", User.class)
                    .setParameter("username", payload.getUsername())
                    .getSingleResult();

            // TODO - need to salt and encrypt the password
            if (user.getPassword().equals(payload.getPassword())) {
                // TODO - is UUID good/unique enough for the token?
                AuthenticationToken token = new AuthenticationToken(UUID.randomUUID().toString(), user.getUsername());
                em.persist(token);

                resp = Response.ok(token).build();
            } else {
                resp = Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (NoResultException | NonUniqueResultException ex) {
            resp = Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        return resp;
    }
    
    @PUT
    @Path("/logout")
    @RolesAllowed({"authenticated-user"})
    public Response logout(@Context final HttpServletRequest request) throws ServletException {
        String token = request.getHeader("X-Auth-Token");
        if (null != token) {
            AuthenticationToken t = em.find(AuthenticationToken.class, token);
            if (null != t) {
                em.remove(t);
            }
        }
        
        request.logout();
        
        return Response.ok().build();
    }

}
