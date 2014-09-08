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

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author nderwin
 */
public class PersonTest {

    private Validator validator;

    @Before
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testConstructor() {
        Person testMe = new Person("", null);
        Set<ConstraintViolation<Person>> violations = validator.validate(testMe);
        assertEquals(violations.size(), 1);
    }

    @Test
    public void testSetGivenName() {
        Person testMe = new Person("Smith", "John");
        testMe.setGivenName(null);
        Set<ConstraintViolation<Person>> violations = validator.validate(testMe);
        assertEquals(violations.size(), 1);
    }
}
