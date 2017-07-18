/*
 * Copyright 2017 Nathan Erwin.
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
package com.github.nderwin.lee7.security.control;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author nderwin
 */
@Singleton
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class TokenCacheTimer {
    
    private static final Logger LOG = Logger.getLogger(TokenCacheTimer.class.getName());
    
    @PersistenceContext
    EntityManager em;
    
    @Lock(LockType.READ)
    @Schedule(persistent = false, hour = "*", minute = "*/15")
    public void execute() {
        int count = em.createNamedQuery("InvalidToken.deleteExpiredTokens").executeUpdate();
        
        LOG.log(Level.INFO, "Invalid cached tokens removed: {0}", count);
    }
}
