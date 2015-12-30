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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import jsr375.identitystore.CredentialValidationResult;
import jsr375.identitystore.IdentityStore;
import jsr375.identitystore.credential.Credential;
import jsr375.identitystore.credential.TokenCredential;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import static jsr375.identitystore.CredentialValidationResult.INVALID_RESULT;
import static jsr375.identitystore.CredentialValidationResult.NOT_VALIDATED_RESULT;

/**
 *
 * @author nderwin
 */
@RequestScoped
public class DefaultIdentityStore implements IdentityStore {
    
    private static final Logger LOG = Logger.getLogger(DefaultIdentityStore.class.getName());

    @Inject
    RsaUtil rsa;
    
    @Override
    public CredentialValidationResult validate(final Credential credential) {
        if (credential instanceof TokenCredential) {
            return validate((TokenCredential) credential);
        }

        return NOT_VALIDATED_RESULT;
    }

    public CredentialValidationResult validate(final TokenCredential credential) {
        try {
            JwtConsumer consumer = new JwtConsumerBuilder()
                    .setRequireExpirationTime()
                    .setAllowedClockSkewInSeconds(30)
                    .setRequireSubject()
                    .setExpectedIssuer(rsa.getIssuer())
                    .setVerificationKey(rsa.getRsaJsonWebKey().getKey())
                    .build();
            
            JwtClaims claims = consumer.processToClaims(credential.getToken());
            
            return new CredentialValidationResult(
                    CredentialValidationResult.Status.VALID, 
                    claims.getSubject(), 
                    (List<String>) claims.getClaimValue("groups"), 
                    (List<String>) claims.getClaimValue("roles")
            );
        } catch (InvalidJwtException | MalformedClaimException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }

        return INVALID_RESULT;
    }
}
