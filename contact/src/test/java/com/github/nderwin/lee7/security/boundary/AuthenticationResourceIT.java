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
package com.github.nderwin.lee7.security.boundary;

import com.github.nderwin.lee7.LogAspect;
import com.github.nderwin.lee7.contact.ApplicationConfig;
import com.github.nderwin.lee7.security.DefaultServerAuthModule;
import com.github.nderwin.lee7.security.entity.Role;
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
import org.jboss.shrinkwrap.resolver.api.Resolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
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
        WebArchive wa = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(ApplicationConfig.class)
                .addClass(LogAspect.class)
                .addPackage(DefaultServerAuthModule.class.getPackage())
                .addPackage(AuthenticationResource.class.getPackage())
                .addPackage(Role.class.getPackage())
                .addPackages(true, "jsr375")
                .addAsResource("persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource("beans.xml")
                .addAsWebInfResource("jboss-web.xml")
                .addAsWebInfResource("web.xml")
                .addAsLibraries(Resolvers
                        .use(MavenResolverSystem.class)
                        .resolve("org.bitbucket.b_c:jose4j:0.4.4")
                        .withTransitivity()
                        .asFile())
                .addAsLibraries(Resolvers
                        .use(MavenResolverSystem.class)
                        .resolve("org.mindrot:jbcrypt:0.4")
                        .withTransitivity()
                        .asFile());

        return wa;
    }

    @Test
    public void testLoginAndLogout() {
        Response result = createTarget("/login")
                .request()
                .post(Entity.json(new LoginPayload("test", "test")));

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
        String jwt = result.readEntity(String.class);
        assertNotNull(jwt);

        result = createTarget("/logout")
                .request()
                .header("Authorization", jwt)
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
