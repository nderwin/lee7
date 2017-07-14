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
package com.github.nderwin.lee7.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 *
 * @author nderwin
 */
@Startup
@Singleton
public class RsaKeystore {

    private RSAPublicKey publicKey;
    
    private RSAPrivateKey privateKey;
    
    @PostConstruct
    public void init() {
        KeyPairGenerator keyGenerator;
        
        try {
            keyGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }

        keyGenerator.initialize(2048);
        
        KeyPair kp = keyGenerator.generateKeyPair();
        
        publicKey = (RSAPublicKey) kp.getPublic();
        privateKey = (RSAPrivateKey) kp.getPrivate();
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

}
