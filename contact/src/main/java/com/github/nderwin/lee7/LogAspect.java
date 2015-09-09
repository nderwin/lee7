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
package com.github.nderwin.lee7;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * A logging aspect that logs method invocations with a level of CONFIG.
 *
 * @author nderwin
 * @see Level
 */
public class LogAspect {
    
    @AroundInvoke
    public Object logAccess(final InvocationContext ic) throws Exception {
        // log as the target class, not the aspect
        Logger.getLogger(ic.getTarget().getClass().getName())
                .log(Level.CONFIG, "method {0}", ic.getMethod().getName());
        
        return ic.proceed();
    }
}
