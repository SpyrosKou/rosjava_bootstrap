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

package org.ros.internal.message.context;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.ros.internal.message.field.Field;
import org.ros.message.MessageDeclarationImpl;
import org.ros.message.MessageFactory;
import org.ros.message.MessageIdentifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Encapsulates the immutable metadata that describes a message type.
 * <p>
 * Note that this class is not thread safe.
 *
 * @author damonkohler@google.com (Damon Kohler)
 */
public final class MessageContext {

    private final MessageDeclarationImpl messageDeclaration;
    private final MessageFactory messageFactory;
    private final Map<String, Supplier<Field>> fieldFactories = Maps.newHashMap();
    private final Map<String, String> fieldGetterNames = Maps.newHashMap();
    private final Map<String, String> fieldSetterNames = Maps.newHashMap();
    private final List<String> fieldNames = Lists.newArrayList();

    public MessageContext(final MessageDeclarationImpl messageDeclaration, final MessageFactory messageFactory) {
        this.messageDeclaration = messageDeclaration;
        this.messageFactory = messageFactory;
    }

    public final MessageFactory getMessageFactory() {
        return messageFactory;
    }

    public final MessageIdentifier getMessageIdentifer() {
        return messageDeclaration.getMessageIdentifier();
    }

    public final String getType() {
        return messageDeclaration.getType();
    }

    public final String getPackage() {
        return messageDeclaration.getPackage();
    }

    public final String getName() {
        return messageDeclaration.getName();
    }

    public final String getDefinition() {
        return messageDeclaration.getDefinition();
    }

    public final void addFieldFactory(String name, final Supplier<Field> fieldFactory) {
        this.fieldFactories.put(name, fieldFactory);
        this.fieldGetterNames.put(name, "get" + getJavaName(name));
        this.fieldSetterNames.put(name, "set" + getJavaName(name));
        this.fieldNames.add(name);
    }

    private final String getJavaName(String name) {
        final String[] parts = name.split("_");
        final StringBuilder fieldName = new StringBuilder();
        for (final String part : parts) {
            fieldName.append(part.substring(0, 1).toUpperCase() + part.substring(1));
        }
        return fieldName.toString();
    }

    public final boolean hasField(String name) {
        // O(1) instead of an O(n) check against the list of field names.
        return this.fieldFactories.containsKey(name);
    }

    public final String getFieldGetterName(String name) {
        return fieldGetterNames.get(name);
    }

    public final String getFieldSetterName(String name) {
        return fieldSetterNames.get(name);
    }

    public final Supplier<Field> getFieldFactory(String name) {
        return fieldFactories.get(name);
    }

    /**
     * @return a {@link List} of field names in the order they were added
     */
    public final List<String> getFieldNames() {
        return Collections.unmodifiableList(fieldNames);
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        final int result = prime * 1 + ((messageDeclaration == null) ? 0 : messageDeclaration.hashCode());
        return result;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MessageContext other = (MessageContext) obj;
        if (messageDeclaration == null) {
            if (other.messageDeclaration != null)
                return false;
        } else if (!messageDeclaration.equals(other.messageDeclaration))
            return false;
        return true;
    }
}
