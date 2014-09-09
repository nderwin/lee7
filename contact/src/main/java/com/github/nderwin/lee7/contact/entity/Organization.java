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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An organization, such as a company or a non-profit group.
 *
 * @author nderwin
 */
@Entity(name = "Organization")
@Table(schema = "contact", name = "organization")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Organization extends LegalEntity implements Serializable {

    private static final long serialVersionUID = -7797691064923370946L;

    @Basic(optional = false)
    @Column(name = "name", nullable = false, length = 50)
    @NotNull
    @Size(min = 1, max = 50)
    @XmlElement(nillable = false, required = true)
    private String name;

    protected Organization() {
    }

    public Organization(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Organization other = (Organization) obj;
        return Objects.equals(this.name, other.name);
    }

    @Override
    public String toString() {
        return "Company{" + "name=" + name + '}';
    }
}
