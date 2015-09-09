/*
 * Copyright 2015 Nathan Erwin.
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
package com.github.nderwin.lee7.authentication.boundary;

import com.github.nderwin.lee7.LogAspect;
import com.github.nderwin.lee7.authentication.HttpServerAuthModule;
import com.github.nderwin.lee7.authentication.entity.AuthenticationToken;
import com.github.nderwin.lee7.contact.ApplicationConfig;
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
public class AuthenticationResourceIT {
    
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
        wa.addAsResource("persistence.xml", "META-INF/persistence.xml");
        wa.addAsWebInfResource("beans.xml");
        wa.addAsWebInfResource("jboss-web.xml");
        wa.addAsWebInfResource("web.xml");

        return wa;
    }

    @Test
    public void testLoginAndLogout() {
        Response result = createTarget("/login")
                .request()
                .post(Entity.json(new LoginPayload("test", "test")));

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
        AuthenticationToken token = result.readEntity(AuthenticationToken.class);
        assertNotNull(token);
        assertEquals("test", token.getUsername());
        assertNotNull(token.getToken());

        result = createTarget("/logout")
                .request()
                .header("X-Auth-Token", token.getToken())
                .put(null);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
    }
    
    @Test
    public void testInvalidPassword() {
        Response result = createTarget("/login")
                .request()
                .post(Entity.json(new LoginPayload("test", "")));

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), result.getStatus());
    }
    
    @Test
    public void testInvalidUser() {
        Response result = createTarget("/login")
                .request()
                .post(Entity.json(new LoginPayload("", "")));

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), result.getStatus());
    }

    private WebTarget createTarget(final String path) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(contextPath.toExternalForm())
                .path(ApplicationConfig.class.getAnnotation(ApplicationPath.class).value())
                .path(AuthenticationResource.class.getAnnotation(Path.class).value())
                .path(path);
        
        return target;
    }
}
