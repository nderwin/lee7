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

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An individual person.
 *
 * @author nderwin
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity(name = "Person")
@Table(schema = "contact", name = "person")
public class Person extends LegalEntity implements Serializable {

    private static final long serialVersionUID = -6010293547035868894L;

    @Basic
    @Column(name = "surname")
    private String surname;

    @Basic(optional = false)
    @Column(name = "givenname", nullable = false)
    private String givenName;

    protected Person() {
    }

    public Person(final String surname, final String givenName) {
        if (givenName == null || givenName.isEmpty()) {
            throw new IllegalArgumentException("givenName must contain a value");
        }

        this.surname = surname;
        this.givenName = givenName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(final String surname) {
        this.surname = surname;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(final String givenName) {
        if (givenName == null || givenName.isEmpty()) {
            throw new IllegalArgumentException("givenName must contain a value");
        }

        this.givenName = givenName;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.surname);
        hash = 53 * hash + Objects.hashCode(this.givenName);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Person other = (Person) obj;
        if (!Objects.equals(this.surname, other.surname)) {
            return false;
        }
        return Objects.equals(this.givenName, other.givenName);
    }

    @Override
    public String toString() {
        return "Person{" + "surname=" + surname + ", givenName=" + givenName + '}';
    }
}
