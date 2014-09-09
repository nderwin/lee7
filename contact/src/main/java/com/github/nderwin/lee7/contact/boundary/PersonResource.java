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

import com.github.nderwin.lee7.contact.entity.Person;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * A RESTful web service that provides CRUD operations for people.
 *
 * @author nderwin
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Path("people")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonResource {

    private static final Logger LOG = Logger.getLogger(PersonResource.class.getName());

    @PersistenceContext
    EntityManager em;

    @GET
    @Path("/")
    public PagingListWrapper<Person> getAll(
            @DefaultValue("0") @QueryParam("limit") final int limit, 
            @DefaultValue("0") @QueryParam("offset") final int offset) {
        
        try {
            TypedQuery<Person> q = em.createQuery("SELECT p FROM Person p", Person.class)
                    .setFirstResult(offset)
                    .setMaxResults(limit);
            return new PagingListWrapper<>(limit, offset, q.getResultList());
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
            return new PagingListWrapper<>(Collections.EMPTY_LIST);
        }
    }
    
    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") final Long id) {
        Person p = em.find(Person.class, id);
        return Response.ok(p).build();
    }

    @POST
    @Path("/")
    public Response save(final Person person) throws URISyntaxException {
        try {
            em.persist(person);
            return Response.created(new URI(getClass().getAnnotation(Path.class).value() + "/" + person.getId().toString())).build();
        } catch (PersistenceException ex) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(final Person person) {
        try {
            Person p = em.find(Person.class, person.getId());
            if (null == p) {
                throw new EntityNotFoundException("No Person found for id " + person.getId());
            }
            
            em.merge(person);
            return Response.noContent().build();
        } catch (IllegalArgumentException | PersistenceException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    
    @DELETE
    @Path("/{id}")
    public Response delete(final Person person) {
        try {
            Person p = em.find(Person.class, person.getId());
            if (null == p) {
                throw new EntityNotFoundException("No Person found for id " + person.getId());
            }
            
            em.remove(p);
            return Response.ok().build();
        } catch (PersistenceException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
