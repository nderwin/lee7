/*
 * Copyright 2014 nde.
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
package nde.contact.boundary;

import java.util.Collection;
import java.util.Collections;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import nde.contact.entity.Organization;
import nde.contact.entity.LegalEntity;
import nde.contact.entity.Person;

/**
 * A RESTful web service that provides CRUD operations for contacts.
 *
 * @author nde
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Path("contacts")
@Consumes(MediaType.APPLICATION_JSON)
public class ContactResource {

    @PersistenceContext
    EntityManager em;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        Collection<LegalEntity> les = Collections.EMPTY_LIST;

        les.addAll(em.createQuery("SELECT p FROM Person p", Person.class).getResultList());
        les.addAll(em.createQuery("SELECT o FROM Organization o", Organization.class).getResultList());

        return Response.ok(les).build();
    }

    @GET
    @Path("person/{id}")
    public Response getPerson(@PathParam("id") final Long id) {
        Person p = em.find(Person.class, id);
        return Response.ok(p).build();
    }

    @GET
    @Path("organization/{id}")
    public Response getOrganization(@PathParam("id") final Long id) {
        Organization c = em.find(Organization.class, id);
        return Response.ok(c).build();
    }

    @PUT
    public Response put(final LegalEntity entity) {
        em.persist(entity);

        return Response.status(Response.Status.ACCEPTED).entity(entity).build();
    }
}
