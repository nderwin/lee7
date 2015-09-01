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
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author nderwin
 */
@Entity(name = "AuthenticationToken")
@Table(schema = "contact", name = "authentication_token")
public class AuthenticationToken implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "token", nullable = false, length = 255)
    private String token;

    @Basic(optional = false)
    @Column(name = "username", nullable = false, length = 255)
    private String username;

    @Basic(optional = false)
    @Column(name = "creationdate", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @Basic(optional = true)
    @Column(name = "lastuseddate", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUsedDate;

    protected AuthenticationToken() {
    }

    public AuthenticationToken(final String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastUsedDate() {
        return lastUsedDate;
    }

    public void setLastUsedDate(final Date lastUsedDate) {
        this.lastUsedDate = lastUsedDate;
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the token fields are not set
        if (!(object instanceof AuthenticationToken)) {
            return false;
        }
        AuthenticationToken other = (AuthenticationToken) object;
        if ((this.token == null && other.token != null) || (this.token != null && !this.token.equals(other.token))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.github.nderwin.lee7.authentication.entity.AuthenticationToken[ id=" + token + " ]";
    }

}
