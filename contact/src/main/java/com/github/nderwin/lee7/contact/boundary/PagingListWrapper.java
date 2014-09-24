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

package com.github.nderwin.lee7.contact.boundary;

import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class wraps collections of resources so that they can be navigated
 * in small chunks, rather than returning an entire dataset to the client.
 *
 * @author nderwin
 * @param <T> the type of resource that is being paged
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PagingListWrapper<T> {

    @XmlElement
    private int limit;
    
    @XmlElement
    private int offset;
    
    @XmlElement
    private List<T> data;
    
    public PagingListWrapper() {
        this(0, 0, Collections.EMPTY_LIST);
    }

    public PagingListWrapper(final List<T> data) {
        this(0, 0, data);
    }

    public PagingListWrapper(final int limit, final int offset, final List<T> data) {
        if (0 > offset) {
            throw new IllegalArgumentException();
        }
        
        this.limit = limit;
        this.offset = offset;
        this.data = data;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public List<T> getData() {
        return data;
    }
}
