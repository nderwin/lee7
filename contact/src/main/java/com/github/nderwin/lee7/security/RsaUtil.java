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
package com.github.nderwin.lee7.security;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.lang.JoseException;

/**
 *
 * @author nderwin
 */
@Startup
@Singleton
public class RsaUtil {

    private static final Logger LOG = Logger.getLogger(RsaUtil.class.getName());
    
    private static final String ISSUER = "lee7-security";
    
    private RsaJsonWebKey rsaJsonWebKey;
    
    @PostConstruct
    public void init() {
        try {
            rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
            rsaJsonWebKey.setKeyId("id");
        } catch (JoseException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public RsaJsonWebKey getRsaJsonWebKey() {
        return rsaJsonWebKey;
    }

    public String getIssuer() {
        return ISSUER;
    }
}
