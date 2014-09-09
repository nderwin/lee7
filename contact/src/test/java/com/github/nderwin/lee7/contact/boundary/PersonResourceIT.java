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
import java.lang.reflect.Field;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 *
 * @author nderwin
 */
@RunWith(Arquillian.class)
public class PersonResourceIT {

    @Inject
    private PersonResource testMe;

    @Inject
    private UserTransaction utx;

    @PersistenceContext
    EntityManager em;

    private Person seedPerson;

    public PersonResourceIT() {
    }

    @Deployment
    public static Archive<?> createDeployment() {
        WebArchive wa = ShrinkWrap.create(WebArchive.class, "test.war");
        wa.addClass(PersonResource.class);
        wa.addClass(PagingListWrapper.class);
        wa.addPackage(Person.class.getPackage());
        wa.addAsResource("persistence.xml", "META-INF/persistence.xml");
        wa.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return wa;
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws Exception {
        utx.begin();
        em.joinTransaction();

        seedPerson = new Person("Person", "Seed");
        em.persist(seedPerson);

        utx.commit();
    }

    @After
    public void tearDown() throws Exception {
        utx.begin();
        em.joinTransaction();

        seedPerson = em.merge(seedPerson);
        em.remove(seedPerson);

        utx.commit();
    }

    @Test
    public void testGet() throws Exception {
        Response result = testMe.get(seedPerson.getId());
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
        assertNotNull(result.getEntity());
        assertEquals(seedPerson.getSurname(), ((Person) result.getEntity()).getSurname());
        assertEquals(seedPerson.getGivenName(), ((Person) result.getEntity()).getGivenName());
    }

    @Test
    public void testPost() throws Exception {
        Response result = testMe.save(new Person("Person", "Test"));
        assertEquals(Response.Status.CREATED.getStatusCode(), result.getStatus());
        assertTrue(result.getHeaders().containsKey("Location"));
        assertTrue(result.getHeaderString("Location").matches("^.*people/[0-9]*$"));
    }

    @Test
    public void testPostWithId() throws Exception {
        Person org = createWithId(Long.MAX_VALUE, "Person", "Test");

        Response result = testMe.save(org);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }
    
    @Test
    public void testPut() throws Exception {
        Person org = new Person("Person", "Test");
        Response result = testMe.save(org);

        Long id = Long.parseLong(result.getHeaderString("Location").split("people/")[1]);
        org = (Person) testMe.get(id).getEntity();
        org.setGivenName("Updated");

        result = testMe.update(org);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), result.getStatus());

        org = (Person) testMe.get(id).getEntity();
        assertEquals("Updated", org.getGivenName());
    }

    @Test
    public void testPutNotExisting() throws Exception {
        Person org = createWithId(Long.MIN_VALUE, "Person", "Test");

        Response result = testMe.update(org);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }
    
    @Test
    public void testPutNewInstance() {
        Response result = testMe.update(new Person("Person", "Foo"));
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }
    
    @Test
    public void testDelete() throws Exception {
        Person org = new Person("Person", "Delete");
        Response result = testMe.save(org);

        Long id = Long.parseLong(result.getHeaderString("Location").split("people/")[1]);
        org = (Person) testMe.get(id).getEntity();

        result = testMe.delete(org);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
    }
    
    @Test
    public void testDeleteNonExisting() throws Exception {
        Person org = createWithId(Long.MIN_VALUE, "Person", "Delete");

        Response result = testMe.delete(org);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }
    
    @Test
    public void testGetAll() throws Exception {
        PagingListWrapper<Person> result = testMe.getAll(50, 0);
        assertNotNull(result);
        assertNotNull(result.getData());
        assertFalse(result.getData().isEmpty());
    }
    
    @Test
    public void testGetAllBadLimit() {
        PagingListWrapper<Person> result = testMe.getAll(-50, 0);
        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getLimit());
    }
    
    @Test
    public void testGetAllBadOffset() {
        PagingListWrapper<Person> result = testMe.getAll(50, -1);
        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getOffset());
    }
    
    private Person createWithId(final Long id, final String surname, final String givenName) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Person org = new Person(surname, givenName);
        
        // force an id into the object we want persisted
        Field f = org.getClass().getSuperclass().getDeclaredField("id");
        f.setAccessible(true);
        f.set(org, id);

        return org;
    }
}
