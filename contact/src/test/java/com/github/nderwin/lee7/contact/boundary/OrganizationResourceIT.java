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
import java.io.Serializable;
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
public class OrganizationResourceIT implements Serializable {

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
    public void testGetOrganization() throws Exception {
        Response result = testMe.getOrganization(seedOrg.getId());
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
        assertNotNull(result.getEntity());
        assertEquals(seedOrg.getName(), ((Organization) result.getEntity()).getName());
    }

    @Test
    public void testPostOrganization() throws Exception {
        Response result = testMe.postOrganization(new Organization("Test Org"));
        assertEquals(Response.Status.CREATED.getStatusCode(), result.getStatus());
        assertTrue(result.getHeaders().containsKey("Location"));
        assertTrue(result.getHeaderString("Location").matches("^.*organizations/[0-9]*$"));
    }

    @Test
    public void testPostOrganizationWithId() throws Exception {
        Organization org = new Organization("Test Org");

        // force an id into the object we want persisted
        Field f = org.getClass().getSuperclass().getDeclaredField("id");
        f.setAccessible(true);
        f.set(org, 12345L);

        Response result = testMe.postOrganization(org);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }
}
