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
import com.github.nderwin.lee7.authentication.UserInformation;
import com.github.nderwin.lee7.authentication.entity.AuthenticationToken;
import com.github.nderwin.lee7.authentication.entity.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;

/**
 *
 * @author nderwin
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Interceptors(LogAspect.class)
public class AuthenticationHelper {

    @PersistenceContext
    EntityManager em;

    public UserInformation validateToken(final String token) {
        AuthenticationToken auth = em.find(AuthenticationToken.class, token);
        UserInformation info = null;
        
        if (null != auth) {
            try {
                User user = em.createNamedQuery("findByUsername", User.class)
                        .setParameter("username", auth.getUsername())
                        .getSingleResult();

                List<String> roles = new ArrayList<>(user.getRoles().size());
                user.getRoles().stream().forEach((r) -> {
                    roles.add(r.getName());
                });
                
                info = new UserInformation(user.getUsername(), user.getPassword(), roles.toArray(new String[]{}));
                auth.setLastUsedDate(new Date());
                em.persist(auth);
            } catch (NoResultException | NonUniqueResultException ex) {
            }
        }
        
        return info;
    }
}
