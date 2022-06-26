/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.internal.message;

import com.google.common.annotations.VisibleForTesting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public final class DefaultMessageInterfaceClassProvider implements MessageInterfaceClassProvider {

    private final Map<String, Class<?>> cache = new ConcurrentHashMap<>();


    @SuppressWarnings("unchecked")
    @Override
    public final <T> Class<T> get(final String messageType) {
        return (Class<T>) this.cache.computeIfAbsent(messageType, this::create);
    }

    private final <T> Class<T> create(final String messageType) {
        try {
            final String className = messageType.replace("/", ".");
            final Class<T> messageInterfaceClass = (Class<T>) this.getClass().getClassLoader().loadClass(className);
            return messageInterfaceClass;
        } catch (ClassNotFoundException e) {
            return (Class<T>) RawMessage.class;
        }
    }

    @VisibleForTesting
    final <T> void add(final String messageType,final Class<T> messageInterfaceClass) {
        this.cache.put(messageType, messageInterfaceClass);
    }
}
