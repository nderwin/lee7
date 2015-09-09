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
import com.github.nderwin.lee7.authentication.HttpServerAuthModule;
import com.github.nderwin.lee7.authentication.boundary.AuthenticationResource;
import com.github.nderwin.lee7.authentication.entity.AuthenticationToken;
import com.github.nderwin.lee7.contact.ApplicationConfig;
import com.github.nderwin.lee7.contact.entity.Person;
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
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 *
 * @author nderwin
 */
@RunWith(Arquillian.class)
@RunAsClient
public class PersonResourceIT {

    private static final String TOKEN = "12345";
    
    @ArquillianResource 
    private URL contextPath;
    
    @Deployment
    public static Archive<?> createDeployment() {
        WebArchive wa = ShrinkWrap.create(WebArchive.class, "test.war");
        wa.addClass(ApplicationConfig.class);
        wa.addClass(LogAspect.class);
        wa.addPackage(HttpServerAuthModule.class.getPackage());
        wa.addPackage(AuthenticationResource.class.getPackage());
        wa.addPackage(AuthenticationToken.class.getPackage());
        wa.addPackage(PersonResource.class.getPackage());
        wa.addPackage(Person.class.getPackage());
        wa.addAsResource("persistence.xml", "META-INF/persistence.xml");
        wa.addAsWebInfResource("beans.xml");
        wa.addAsWebInfResource("jboss-web.xml");
        wa.addAsWebInfResource("web.xml");

        return wa;
    }
    
    @Test
    public void testPost() {
        Response result = createTarget()
                .request()
                .header("X-Auth-Token", TOKEN)
                .post(Entity.json(new Person("Person", "Test")));

        assertEquals(Response.Status.CREATED.getStatusCode(), result.getStatus());
        assertTrue(result.getHeaders().containsKey("Location"));
        assertTrue(result.getHeaderString("Location").matches("^.*" + PersonResource.class.getAnnotation(Path.class).value() + "/[0-9]*$"));
    }
    
    @Test
    public void testGet() {
        Person p = postAndGet("Person", "Test");

        assertNotNull(p);
    }
    
    @Test
    public void testPut() {
        Person p = postAndGet("Person", "Test");

        // update it
        p.setGivenName("Updated");

        Response result = createTarget()
                .path(p.getId().toString())
                .request()
                .header("X-Auth-Token", TOKEN)
                .put(Entity.json(p));
        
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), result.getStatus());

        // make sure it was updated
        result = createTarget()
                .path(p.getId().toString())
                .request()
                .header("X-Auth-Token", TOKEN)
                .get();

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
        p = result.readEntity(Person.class);
        assertNotNull(p);
        assertEquals("Updated", p.getGivenName());
    }
    
    @Test
    public void testPutDifferentEntityId() {
        Person p = postAndGet("Person", "Test");

        Response result = createTarget()
                .path(p.getId().toString())
                .request()
                .header("X-Auth-Token", TOKEN)
                .put(Entity.json(createWithId(p.getId() + 1L, "Person", "Oops")));
        
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }

    @Test
    public void testPutDifferentPathId() {
        Person p = postAndGet("Person", "Test");

        Response result = createTarget()
                .path("" + (p.getId() + 1L))
                .request()
                .header("X-Auth-Token", TOKEN)
                .put(Entity.json(createWithId(p.getId(), "Person", "Oops")));
        
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }

    @Test
    public void testGetAll() {
        postAndGet("Person", "Test");
        
        Response resp = createTarget()
                .queryParam("limit", 50)
                .queryParam("offset", 0)
                .request()
                .header("X-Auth-Token", TOKEN)
                .get();

        PagingListWrapper<Person> result = resp.readEntity(PagingListWrapper.class);
        
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        assertNotNull(result);
        assertNotNull(result.getData());
        assertNotEquals(0, result.getData().size());
        assertFalse(result.getData().isEmpty());
    }
    
    @Test
    public void testDelete() {
        Person p = postAndGet("Person", "Test");
        
        Response result = createTarget()
                .path(p.getId().toString())
                .request()
                .header("X-Auth-Token", TOKEN)
                .delete();

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
        
        result = createTarget()
                .path(p.getId().toString())
                .request()
                .header("X-Auth-Token", TOKEN)
                .get();

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }

    @Test
    public void testPostWithId() {
        Person org = createWithId(Long.MAX_VALUE, "Person", "Test");

        Response resp = createTarget()
                .request()
                .header("X-Auth-Token", TOKEN)
                .post(Entity.json(org));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), resp.getStatus());
    }
    
    @Test
    public void testPutNotExisting() {
        Person person = createWithId(Long.MIN_VALUE, "Person", "Test");

        Response result = createTarget()
                .path(person.getId().toString())
                .request()
                .header("X-Auth-Token", TOKEN)
                .put(Entity.json(person));
        
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }
    
    @Test
    public void testPutNewInstance() {
        Response result = createTarget()
                .path("" + Long.MAX_VALUE)
                .request()
                .header("X-Auth-Token", TOKEN)
                .put(Entity.json(new Person("Person", "Foo")));
        
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }
    
    @Test
    public void testDeleteNonExisting() {
        Response result = createTarget()
                .path("" + Long.MIN_VALUE)
                .request()
                .header("X-Auth-Token", TOKEN)
                .delete();

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), result.getStatus());
    }
    
    @Test
    public void testGetAllBadLimit() {
        Response resp = createTarget()
                .queryParam("limit", -50)
                .queryParam("offset", 0)
                .request()
                .header("X-Auth-Token", TOKEN)
                .get();

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
    }
    
    @Test
    public void testGetAllBadOffset() {
        Response resp = createTarget()
                .queryParam("limit", 50)
                .queryParam("offset", -1)
                .request()
                .header("X-Auth-Token", TOKEN)
                .get();
        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
    }
    
    private Person createWithId(final Long id, final String surname, final String givenName) {
        Person org = new Person(surname, givenName);

        try {
            // force an id into the object we want persisted
            Field f = org.getClass().getSuperclass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(org, id);
        } catch (Exception ex) {
        }

        return org;
    }
    
    private WebTarget createTarget() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(contextPath.toExternalForm())
                .path(ApplicationConfig.class.getAnnotation(ApplicationPath.class).value())
                .path(PersonResource.class.getAnnotation(Path.class).value());
        
        return target;
    }
    
    private Person postAndGet(final String surname, final String givenName) {
        Response result = createTarget()
                .request()
                .header("X-Auth-Token", TOKEN)
                .post(Entity.json(new Person(surname, givenName)));

        assertEquals(Response.Status.CREATED.getStatusCode(), result.getStatus());

        String id = result.getHeaderString("Location").split(PersonResource.class.getAnnotation(Path.class).value() + "/")[1];
        result = createTarget()
                .path(id)
                .request()
                .header("X-Auth-Token", TOKEN)
                .get();

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        return result.readEntity(Person.class);
    }
}
