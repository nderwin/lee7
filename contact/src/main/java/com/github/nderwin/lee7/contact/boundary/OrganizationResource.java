/*
 * Copyright 2014 Nathan Erwin.
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
package com.github.nderwin.lee7.contact.boundary;

import com.github.nderwin.lee7.LogAspect;
import com.github.nderwin.lee7.contact.entity.Organization;
import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.security.RolesAllowed;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * A RESTful web service that provides CRUD operations for organizations.
 *
 * @author nderwin
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Path("resources/organizations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Interceptors(LogAspect.class)
@RolesAllowed({"authenticated-user"})
public class OrganizationResource {
    
    @PersistenceContext
    EntityManager em;

    @GET
    @Path("/")
    public Response getAll(
            @DefaultValue("50") @QueryParam("limit") final int limit, 
            @DefaultValue("0") @QueryParam("offset") final int offset) {
        
        TypedQuery<Organization> q = em.createQuery("SELECT o FROM Organization o", Organization.class)
                .setFirstResult(offset)
                .setMaxResults(limit);
        return Response.ok(new PagingListWrapper<>(limit, offset, q.getResultList())).build();
    }
    
    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") final Long id) {
        Organization org = em.find(Organization.class, id);
        
        if (null == org) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        return Response.ok(org).build();
    }
    
    @POST
    @Path("/")
    public Response save(@Context final HttpServletRequest request, final Organization organization) throws URISyntaxException {
        em.persist(organization);
        return Response.created(URI.create(request.getRequestURI() + "/" + organization.getId().toString())).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") final Long id, final Organization organization) {
        Organization org = em.find(Organization.class, id);
        
        if ((null == org) || (!org.getId().equals(organization.getId()))) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        em.merge(organization);
        return Response.noContent().build();
    }
    
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") final Long id) {
        Organization org = em.find(Organization.class, id);
        
        if (null == org) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        em.remove(org);
        return Response.ok().build();
    }
}
