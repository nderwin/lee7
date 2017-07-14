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
package com.github.nderwin.lee7.contact;

import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;
import javax.persistence.PersistenceException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author nderwin
 */
@Provider
public class ResourceExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(ResourceExceptionMapper.class.getName());
    
    @Override
    public Response toResponse(Exception exception) {
        Throwable ex = exception;
        if (exception instanceof EJBException) {
            ex = exception.getCause();
            
            if (null == ex) {
                ex = exception;
            }
        }
        
        Response resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exception.getMessage()).build();
        Level level = Level.SEVERE;

        if (ex instanceof IllegalArgumentException) {
            level = Level.WARNING;
            resp = Response.status(Response.Status.BAD_REQUEST).build();
        } else if (ex instanceof PersistenceException) {
            level = Level.WARNING;
            resp = Response.status(Response.Status.NOT_FOUND).build();
        } else if (ex instanceof URISyntaxException) {
            level = Level.WARNING;
        } else if (ex instanceof EJBAccessException) {
            level = Level.WARNING;
            resp = Response.status(Response.Status.UNAUTHORIZED).build();
        } else if (ex instanceof NotFoundException) {
            level = Level.INFO;
            resp = Response.status(Response.Status.NOT_FOUND).build();
        } else if (ex instanceof NotAllowedException) {
            level = Level.INFO;
            resp = Response.status(Response.Status.METHOD_NOT_ALLOWED).build();
        }

        if (Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() == resp.getStatus()) {
            LOG.log(Level.SEVERE, "Exception of type {0} not mapped", ex.getClass().getTypeName());
        }
        
        if (level.intValue() > Level.INFO.intValue()) {
            LOG.log(level, ex.getMessage(), ex);
        } else {
            LOG.log(level, ex.getMessage());
        }
        return resp;
    }
}
