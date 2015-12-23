/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package jsr375.identitystore;

import java.util.Collections;
import java.util.List;

import static jsr375.identitystore.CredentialValidationResult.Status.INVALID;
import static jsr375.identitystore.CredentialValidationResult.Status.NOT_VALIDATED;
import static jsr375.identitystore.CredentialValidationResult.Status.VALID;

/**
 * 
 * @see https://github.com/arjantijms/mechanism-to-store-x/blob/master/jsr375/src/main/java/javax/security/identitystore/CredentialValidationResult.java
 */
public class CredentialValidationResult {

    public static final CredentialValidationResult INVALID_RESULT = new CredentialValidationResult(INVALID, null, null, null);

    public static final CredentialValidationResult NOT_VALIDATED_RESULT = new CredentialValidationResult(NOT_VALIDATED, null, null, null);

    private final String callerName;
    
    private final Status status;
    
    private final List<String> roles;
    
    private final List<String> groups;
    
    public enum Status {
        NOT_VALIDATED,
        INVALID,
        VALID
    }
    
    public CredentialValidationResult(Status status, String callerName, List<String> groups) {
        this(status, callerName, groups, null);
    }
    
    public CredentialValidationResult(Status status, String callerName, List<String> groups, List<String> roles) {
        if (null == status) {
            throw new NullPointerException("status");
        }
        
        this.status = status;
        this.callerName = callerName;
        
        if (VALID == status) {
            if (null != groups) {
                groups = Collections.unmodifiableList(groups);
            }
            this.groups = groups;
            
            if (null != roles) {
                roles = Collections.unmodifiableList(roles);
            }
            this.roles = roles;
        } else {
            this.groups = null;
            this.roles = null;
        }
    }

    public String getCallerName() {
        return callerName;
    }

    public Status getStatus() {
        return status;
    }

    public List<String> getCallerRoles() {
        return roles;
    }

    public List<String> getCallerGroups() {
        return groups;
    }
}
