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

import com.github.nderwin.lee7.contact.ApplicationConfig;
import com.github.nderwin.lee7.contact.entity.Organization;
import java.lang.reflect.Field;
import java.net.URL;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 *
 * @author nderwin
 */
@RunWith(Arquillian.class)
@RunAsClient
public class OrganizationResourceIT {

    static String crudId;
    
    static Organization crudOrg;
    
    @ArquillianResource
    private URL contextPath;
    
    private WebTarget target;

    public OrganizationResourceIT() {
    }

    @Deployment
    public static Archive<?> createDeployment() {
        WebArchive wa = ShrinkWrap.create(WebArchive.class, "test.war");
        wa.addClass(ApplicationConfig.class);
        wa.addPackage(OrganizationResource.class.getPackage());
        wa.addPackage(Organization.class.getPackage());
        wa.addAsResource("persistence.xml", "META-INF/persistence.xml");
        wa.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return wa;
    }

    @Before
    public void setUp() {
        Client client = ClientBuilder.newClient();
        target = client.target(contextPath.toExternalForm())
                .path(ApplicationConfig.class.getAnnotation(ApplicationPath.class).value())
                .path(OrganizationResource.class.getAnnotation(Path.class).value());
    }

    @Test
    @InSequence(0)
    public void testPost() throws Exception {
        Response result = target.request().post(Entity.json(new Organization("Test Org")));

        assertEquals(Response.Status.CREATED.getStatusCode(), result.getStatus());
        assertTrue(result.getHeaders().containsKey("Location"));
        assertTrue(result.getHeaderString("Location").matches("^.*" + OrganizationResource.class.getAnnotation(Path.class).value() + "/[0-9]*$"));
        
        crudId = result.getHeaderString("Location").split(OrganizationResource.class.getAnnotation(Path.class).value() + "/")[1];
    }
    
    @Test
    @InSequence(1)
    public void testGet() throws Exception {
        target = target.path(crudId);

        Response result = target.request().get();
        
        if (result.hasEntity()) {
            crudOrg = result.readEntity(Organization.class);
        }

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
        assertNotNull(crudOrg);
    }

    @Test
    @InSequence(2)
    public void testPut() throws Exception {
        target = target.path(crudId);

        crudOrg.setName("Updated Org");

        Response result = target.request().put(Entity.json(crudOrg));
        
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), result.getStatus());
    }

    @Test
    @InSequence(3)
    public void testGetAfterPut() throws Exception {
        target = target.path(crudId);
        
        Response result = target.request().get();
        
        if (result.hasEntity()) {
            crudOrg = result.readEntity(Organization.class);
        }
        
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
        assertNotNull(crudOrg);
        
        assertEquals("Updated Org", crudOrg.getName());
    }

    @Test
    @InSequence(4)
    public void testPutDifferentEntityId() throws Exception {
        target = target.path(crudId);
        
        Response result = target.request().put(Entity.json(createWithId(crudOrg.getId() + 1L, "Oops")));
        
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }

    @Test
    @InSequence(5)
    public void testPutDifferentPathId() throws Exception {
        target = target.path("" + (crudOrg.getId() + 1L));
        
        Response result = target.request().put(Entity.json(createWithId(crudOrg.getId(), "Oops")));
        
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }

    @Test
    @InSequence(6)
    public void testGetAll() throws Exception {
        target = target.queryParam("limit", 50).queryParam("offset", 0);

        Response resp = target.request().get();

        PagingListWrapper<Organization> result = resp.readEntity(PagingListWrapper.class);

        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        assertNotNull(result);
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertFalse(result.getData().isEmpty());
    }

    @Test
    @InSequence(7)
    public void testDelete() throws Exception {
        target = target.path(crudId);
        
        Response result = target.request().delete();

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
    }

    @Test
    @InSequence(8)
    public void testGetAfterDelete() throws Exception {
        target = target.path(crudId);
        
        Response result = target.request().get();
        
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
        
        crudId = null;
        crudOrg = null;
    }

    @Test
    public void testPostWithId() throws Exception {
        Organization org = createWithId(Long.MAX_VALUE, "Test Org");
        
        Response resp = target.request().post(Entity.json(org));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), resp.getStatus());
    }

    @Test
    public void testPutNotExisting() throws Exception {
        Organization org = createWithId(Long.MIN_VALUE, "Test Org");

        target = target.path(org.getId().toString());
        
        Response result = target.request().put(Entity.json(org));
        
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }

    @Test
    public void testPutNewInstance() {
        target = target.path("" + Long.MAX_VALUE);

        Response result = target.request().put(Entity.json(new Organization("Foo")));
        
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }

    @Test
    public void testDeleteNonExisting() throws Exception {
        target = target.path("" + Long.MIN_VALUE);
        
        Response result = target.request().delete();
        
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }

    @Test
    public void testGetAllBadLimit() {
        target = target.queryParam("limit", -50).queryParam("offset", 0);

        Response resp = target.request().get();

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
    }

    @Test
    public void testGetAllBadOffset() {
        target = target.queryParam("limit", 50).queryParam("offset", -1);

        Response resp = target.request().get();

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
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
