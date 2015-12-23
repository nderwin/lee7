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
import com.github.nderwin.lee7.security.DefaultServerAuthModule;
import com.github.nderwin.lee7.security.boundary.AuthenticationResource;
import com.github.nderwin.lee7.contact.ApplicationConfig;
import com.github.nderwin.lee7.contact.entity.Organization;
import com.github.nderwin.lee7.security.boundary.LoginPayload;
import java.lang.reflect.Field;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
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
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.Resolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
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

    private static final String AUTH_HEADER = "Authorization";
    
    private String token;
    
    @ArquillianResource
    private URL contextPath;

    @Deployment
    public static Archive<?> createDeployment() {
        WebArchive wa = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(ApplicationConfig.class)
                .addClass(LogAspect.class)
                .addPackages(true, "jsr375")
                .addPackages(true, DefaultServerAuthModule.class.getPackage())
                .addPackage(OrganizationResource.class.getPackage())
                .addPackage(Organization.class.getPackage())
                .addAsResource("persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource("beans.xml")
                .addAsWebInfResource("jboss-web.xml")
                .addAsWebInfResource("web.xml")
                .addAsLibraries(Resolvers
                        .use(MavenResolverSystem.class)
                        .resolve("org.bitbucket.b_c:jose4j:0.4.4")
                        .withTransitivity()
                        .asFile());

        return wa;
    }

    @Before
    public void setUp() throws SecurityException, NoSuchMethodException {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(contextPath.toExternalForm())
                .path(ApplicationConfig.class.getAnnotation(ApplicationPath.class).value())
                .path(AuthenticationResource.class.getAnnotation(Path.class).value())
                .path(AuthenticationResource.class.getDeclaredMethod("login", HttpServletRequest.class, LoginPayload.class).getAnnotation(Path.class).value());

        Response resp = target.request()
                .post(Entity.json(new LoginPayload("test", "test")));

        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        
        token = resp.readEntity(String.class);
    }

    @Test
    public void testPost() {
        Response result = createTarget()
                .request()
                .header(AUTH_HEADER, token)
                .post(Entity.json(new Organization("Test Org")));

        assertEquals(Response.Status.CREATED.getStatusCode(), result.getStatus());
        assertTrue(result.getHeaders().containsKey("Location"));
        assertTrue(result.getHeaderString("Location").matches("^.*" + OrganizationResource.class.getAnnotation(Path.class).value() + "/[0-9]*$"));
    }

    @Test
    public void testGet() {
        Organization org = postAndGet("Test Org");

        assertNotNull(org);
    }

    @Test
    public void testPut() {
        Organization org = postAndGet("Test Org");

        // update it
        org.setName("Updated Org");

        Response result = createTarget()
                .path(org.getId().toString())
                .request()
                .header(AUTH_HEADER, token)
                .put(Entity.json(org));

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), result.getStatus());

        // make sure it was updated
        result = createTarget()
                .path(org.getId().toString())
                .request()
                .header(AUTH_HEADER, token)
                .get();

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
        org = result.readEntity(Organization.class);
        assertNotNull(org);
        assertEquals("Updated Org", org.getName());
    }

    @Test
    public void testPutDifferentEntityId() {
        Organization org = postAndGet("Test Org");

        Response result = createTarget()
                .path(org.getId().toString())
                .request()
                .header(AUTH_HEADER, token)
                .put(Entity.json(createWithId(org.getId() + 1L, "Oops")));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }

    @Test
    public void testPutDifferentPathId() {
        Organization org = postAndGet("Test Org");

        Response result = createTarget()
                .path("" + (org.getId() + 1L))
                .request()
                .header(AUTH_HEADER, token)
                .put(Entity.json(createWithId(org.getId(), "Oops")));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }

    @Test
    public void testGetAll() {
        postAndGet("Test Org");

        Response resp = createTarget()
                .queryParam("limit", 50)
                .queryParam("offset", 0)
                .request()
                .header(AUTH_HEADER, token)
                .get();

        PagingListWrapper<Organization> result = resp.readEntity(PagingListWrapper.class);

        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        assertNotNull(result);
        assertNotNull(result.getData());
        assertNotEquals(0, result.getData().size());
        assertFalse(result.getData().isEmpty());
    }

    @Test
    public void testDelete() {
        Organization org = postAndGet("Test Org");

        Response result = createTarget()
                .path(org.getId().toString())
                .request()
                .header(AUTH_HEADER, token)
                .delete();

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        result = createTarget()
                .path(org.getId().toString())
                .request()
                .header(AUTH_HEADER, token)
                .get();

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }

    @Test
    public void testPostWithId() {
        Organization org = createWithId(Long.MAX_VALUE, "Test Org");

        Response resp = createTarget()
                .request()
                .header(AUTH_HEADER, token)
                .post(Entity.json(org));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), resp.getStatus());
    }

    @Test
    public void testPutNotExisting() {
        Organization org = createWithId(Long.MIN_VALUE, "Test Org");

        Response result = createTarget()
                .path(org.getId().toString())
                .request()
                .header(AUTH_HEADER, token)
                .put(Entity.json(org));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }

    @Test
    public void testPutNewInstance() {
        Response result = createTarget()
                .path("" + Long.MAX_VALUE)
                .request()
                .header(AUTH_HEADER, token)
                .put(Entity.json(new Organization("Foo")));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }

    @Test
    public void testDeleteNonExisting() {
        Response result = createTarget()
                .path("" + Long.MIN_VALUE)
                .request()
                .header(AUTH_HEADER, token)
                .delete();

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }

    @Test
    public void testGetAllBadLimit() {
        Response resp = createTarget()
                .queryParam("limit", -50)
                .queryParam("offset", 0)
                .request()
                .header(AUTH_HEADER, token)
                .get();

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
    }

    @Test
    public void testGetAllBadOffset() {
        Response resp = createTarget()
                .queryParam("limit", 50)
                .queryParam("offset", -1)
                .request()
                .header(AUTH_HEADER, token)
                .get();

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
    }

    private Organization createWithId(final Long id, final String name) {
        Organization org = new Organization(name);

        try {
            // force an id into the object we want persisted
            Field f = org.getClass().getSuperclass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(org, id);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
        }

        return org;
    }

    private WebTarget createTarget() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(contextPath.toExternalForm())
                .path(ApplicationConfig.class.getAnnotation(ApplicationPath.class).value())
                .path(OrganizationResource.class.getAnnotation(Path.class).value());

        return target;
    }

    private Organization postAndGet(final String name) {
        Response result = createTarget()
                .request()
                .header(AUTH_HEADER, token)
                .post(Entity.json(new Organization(name)));

        assertEquals(Response.Status.CREATED.getStatusCode(), result.getStatus());

        String id = result.getHeaderString("Location").split(OrganizationResource.class.getAnnotation(Path.class).value() + "/")[1];
        result = createTarget()
                .path(id)
                .request()
                .header(AUTH_HEADER, token)
                .get();

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        return result.readEntity(Organization.class);
    }
}
