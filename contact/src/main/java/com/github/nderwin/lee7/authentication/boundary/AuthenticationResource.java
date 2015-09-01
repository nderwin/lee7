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
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
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
    public Response login(@Context final HttpServletRequest request) {
        
        // get username and password from the payload?
        
        // validate username and password (insensitive search for username)
        
        // generate new auth token, and build response with it
        
        return Response.serverError().entity("{\"IamNot\": \"finishedYet\"}").build();
    }
    
    @PUT
    @Path("/logout")
    @RolesAllowed({"authenticated-user"})
    public Response logout(@Context final HttpServletRequest request) throws ServletException {
        request.logout();
        
        // clear the auth token
        
        return Response.ok().build();
    }
}
