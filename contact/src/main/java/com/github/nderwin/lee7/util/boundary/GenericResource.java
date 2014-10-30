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
package com.github.nderwin.lee7.util.boundary;

import java.net.URI;
import java.net.URISyntaxException;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

/**
 *
 * @author nderwin
 * @param <R>
 */
public class GenericResource<R extends Resource> {
    
    @PersistenceContext
    EntityManager em;

    private final Class<R> resource;
    
    public GenericResource(final Class<R> resource) {
        this.resource = resource;
    }
    
    @GET
    @Path("/")
    public Response list(
            @DefaultValue("50") @QueryParam("limit") final int limit, 
            @DefaultValue("0") @QueryParam("offset") final int offset) {
        
        TypedQuery<R> q = em.createQuery("SELECT r FROM " + resource.getAnnotation(Entity.class).name() + " r", resource)
                .setFirstResult(offset)
                .setMaxResults(limit);
        return Response.ok(new PagingListWrapper<>(limit, offset, q.getResultList())).build();
    }
    
    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") final Long id) {
        R rez = em.find(resource, id);

        if (null == rez) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(new GenericEntity(rez, resource.getClass().getGenericSuperclass())).build();
    }

    @POST
    @Path("/")
    public Response persist(final R rez) throws URISyntaxException {
        em.persist(rez);
        return Response.created(URI.create(getClass().getAnnotation(Path.class).value() + "/" + rez.getId().toString())).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") final Long id, final R rez) {
        R r = em.find(resource, id);

        if ((null == r) || (!r.getId().equals(rez.getId()))) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        em.merge(rez);
        return Response.noContent().build();
    }
    
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") final Long id) {
        R rez = em.find(resource, id);

        if (null == rez) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        em.remove(rez);
        return Response.ok().build();
    }
}
