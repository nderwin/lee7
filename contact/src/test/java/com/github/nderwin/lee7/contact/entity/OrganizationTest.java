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
package com.github.nderwin.lee7.contact.entity;

import java.io.StringWriter;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author nderwin
 */
public class OrganizationTest {

    private Validator validator;

    @Before
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testConstructor() {
        Organization testMe = new Organization(null);
        Set<ConstraintViolation<Organization>> violations = validator.validate(testMe);
        assertEquals(violations.size(), 1);
    }

    @Test
    public void testSetName() {
        Organization testMe = new Organization("ACME Supply Company");
        testMe.setName(null);
        Set<ConstraintViolation<Organization>> violations = validator.validate(testMe);
        assertEquals(violations.size(), 1);
    }
    
    @Test
    public void testSerialization() throws JSONException, JAXBException {
        Organization testMe = new Organization("ACME Supply Company");
        
        JSONObject expected = new JSONObject();
        expected.put("id", JSONObject.NULL);
        expected.put("name", testMe.getName());

        StringWriter sw = new StringWriter();

        JAXBContext jaxbContext = JAXBContext.newInstance(Organization.class);

        Marshaller m = jaxbContext.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty(MarshallerProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);
        m.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
        m.setProperty(MarshallerProperties.JSON_TYPE_COMPATIBILITY, false);
        m.marshal(testMe, sw);
        
        JSONAssert.assertEquals(expected.toString(4), sw.toString(), true);
    }
}
