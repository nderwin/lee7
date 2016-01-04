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
package com.github.nderwin.lee7.security.entity;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.mindrot.BCrypt;

/**
 * Representation of a user of the system.
 *
 * @author nderwin
 */
@Entity(name = "Caller")
@Table(schema = "contact", name = "caller", indexes = {
    @Index(name = "caller_username_idx", unique = true, columnList = "username")
}, uniqueConstraints = {
    @UniqueConstraint(name = "caller_username_UK", columnNames = "username")
})
@NamedQueries(value = {
    @NamedQuery(name = "findByUsername", query = "SELECT c FROM Caller c WHERE LOWER(c.username) = LOWER(:username) ")
})
public class Caller implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(schema = "contact", name = "caller_seq", sequenceName = "caller_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "caller_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "username", nullable = false, length = 255, unique = true)
    private String username;

    @Basic(optional = false)
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(schema = "contact", name = "caller_role", joinColumns = {
        @JoinColumn(name = "caller_id", referencedColumnName = "id")}, inverseJoinColumns = {
        @JoinColumn(name = "role_id", referencedColumnName = "id")})
    private Set<SecurityRole> roles = new HashSet<>();

    protected Caller() {
    }

    public Caller(final String username) {
        if (null == username || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username must be a non-null, non-blank value");
        }

        this.username = username.trim();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public Set<SecurityRole> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public void setRoles(final Set<SecurityRole> roles) {
        this.roles.clear();
        this.roles.addAll(roles);
    }

    public void addRole(final SecurityRole role) {
        this.roles.add(role);
    }

    public void removeRole(final SecurityRole role) {
        this.roles.remove(role);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }

        if (getClass() != object.getClass()) {
            return false;
        }

        final Caller other = (Caller) object;

        return Objects.equals(this.username, other.username);
    }

    @Override
    public String toString() {
        return "Caller{username=" + username + "}";
    }

}
