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
package com.github.nderwin.lee7.authentication.entity;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * A security role, which can be held by any number of users.
 *
 * @author nderwin
 */
@Entity(name = "Role")
@Table(schema = "contact", name = "role", indexes = {
    @Index(name = "role_name_idx", unique = true, columnList = "name")
}, uniqueConstraints = {
    @UniqueConstraint(name = "role_name_UK", columnNames = "name")
})
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(schema = "contact", name = "role_seq", sequenceName = "role_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "name", nullable = false, length = 255, unique = true)
    private String name;
    
    protected Role() {
    }

    public Role(final String name) {
        if (null == name || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name must be non-null and non-blank");
        }
        
        this.name = name.trim();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }

        if (getClass() != object.getClass()) {
            return false;
        }

        final Role other = (Role) object;

        return Objects.equals(this.name, other.name);
    }

    @Override
    public String toString() {
        return "Role{name=" + name + "}";
    }

}
