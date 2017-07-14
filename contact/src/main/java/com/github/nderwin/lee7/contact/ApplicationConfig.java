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

import javax.annotation.security.DeclareRoles;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Minimal configuration for enabling REST Web Services.
 *
 * @author nderwin
 */
@ApplicationPath("resources")
@DeclareRoles({"USER", "ADMIN"})
public class ApplicationConfig extends Application {
}
