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

import jsr375.authenticationmechanism.JaspicUtils;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * A listener for the servlet context to allow registering a custom
 * ServerAuthModule.
 *
 * @author nderwin
 * @see DefaultServerAuthModule
 */
@WebListener
public class StartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        JaspicUtils.registerSAM(sce.getServletContext(), new DefaultServerAuthModule());
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
    }

    public static String getAppContextID(final ServletContext context) {
        return context.getVirtualServerName() + " " + context.getContextPath();
    }
}
