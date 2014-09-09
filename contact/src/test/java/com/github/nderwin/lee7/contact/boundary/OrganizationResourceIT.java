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

import com.github.nderwin.lee7.contact.entity.Organization;
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
public class OrganizationResourceIT {

    @Inject
    private OrganizationResource testMe;

    @Inject
    private UserTransaction utx;

    @PersistenceContext
    EntityManager em;

    private Organization seedOrg;

    public OrganizationResourceIT() {
    }

    @Deployment
    public static Archive<?> createDeployment() {
        WebArchive wa = ShrinkWrap.create(WebArchive.class, "test.war");
        wa.addClass(OrganizationResource.class);
        wa.addClass(PagingListWrapper.class);
        wa.addPackage(Organization.class.getPackage());
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

        seedOrg = new Organization("Seed Organization");
        em.persist(seedOrg);

        utx.commit();
    }

    @After
    public void tearDown() throws Exception {
        utx.begin();
        em.joinTransaction();

        seedOrg = em.merge(seedOrg);
        em.remove(seedOrg);

        utx.commit();
    }

    @Test
    public void testGet() throws Exception {
        Response result = testMe.get(seedOrg.getId());
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
        assertNotNull(result.getEntity());
        assertEquals(seedOrg.getName(), ((Organization) result.getEntity()).getName());
    }

    @Test
    public void testPost() throws Exception {
        Response result = testMe.save(new Organization("Test Org"));
        assertEquals(Response.Status.CREATED.getStatusCode(), result.getStatus());
        assertTrue(result.getHeaders().containsKey("Location"));
        assertTrue(result.getHeaderString("Location").matches("^.*organizations/[0-9]*$"));
    }

    @Test
    public void testPostWithId() throws Exception {
        Organization org = createWithId(Long.MAX_VALUE, "Test Org");

        Response result = testMe.save(org);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }
    
    @Test
    public void testPut() throws Exception {
        Organization org = new Organization("Test Org");
        Response result = testMe.save(org);

        Long id = Long.parseLong(result.getHeaderString("Location").split("organizations/")[1]);
        org = (Organization) testMe.get(id).getEntity();
        org.setName("Updated Org");

        result = testMe.update(org);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), result.getStatus());

        org = (Organization) testMe.get(id).getEntity();
        assertEquals("Updated Org", org.getName());
    }

    @Test
    public void testPutNotExisting() throws Exception {
        Organization org = createWithId(Long.MIN_VALUE, "Test Org");

        Response result = testMe.update(org);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }
    
    @Test
    public void testPutNewInstance() {
        Response result = testMe.update(new Organization("Foo"));
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }
    
    @Test
    public void testDelete() throws Exception {
        Organization org = new Organization("Deleted Org");
        Response result = testMe.save(org);

        Long id = Long.parseLong(result.getHeaderString("Location").split("organizations/")[1]);
        org = (Organization) testMe.get(id).getEntity();

        result = testMe.delete(org);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
    }
    
    @Test
    public void testDeleteNonExisting() throws Exception {
        Organization org = createWithId(Long.MIN_VALUE, "Delete Org");

        Response result = testMe.delete(org);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }
    
    @Test
    public void testGetAll() throws Exception {
        PagingListWrapper<Organization> result = testMe.getAll(50, 0);
        assertNotNull(result);
        assertNotNull(result.getData());
        assertFalse(result.getData().isEmpty());
    }
    
    @Test
    public void testGetAllBadLimit() {
        PagingListWrapper<Organization> result = testMe.getAll(-50, 0);
        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getLimit());
    }
    
    @Test
    public void testGetAllBadOffset() {
        PagingListWrapper<Organization> result = testMe.getAll(50, -1);
        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getOffset());
    }
    
    private Organization createWithId(final Long id, final String name) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Organization org = new Organization(name);
        
        // force an id into the object we want persisted
        Field f = org.getClass().getSuperclass().getDeclaredField("id");
        f.setAccessible(true);
        f.set(org, id);

        return org;
    }
}
