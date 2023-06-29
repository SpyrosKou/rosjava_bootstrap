/*
 * Copyright (C) 2012 Google Inc.
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

package org.ros.internal.message.field;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.ros.exception.RosMessageRuntimeException;
import org.ros.internal.message.context.MessageContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public final class MessageFields {

  private final Map<String, Field> fields=Maps.newHashMap();
  private final Map<String, Field> setters=Maps.newHashMap();
  private final Map<String, Field> getters=Maps.newHashMap();
  private final List<Field> orderedFields=Lists.newArrayList();

  public MessageFields(final MessageContext messageContext) {

    for (final String name : messageContext.getFieldNames()) {
      final Field field = messageContext.getFieldFactory(name).get();
      this.fields.put(name, field);
      this.getters.put(messageContext.getFieldGetterName(name), field);
      this.setters.put(messageContext.getFieldSetterName(name), field);
      this.orderedFields.add(field);
    }
  }

  public final Field getField(String name) {
    return fields.get(name);
  }

  public final Field getSetterField(String name) {
    return setters.get(name);
  }

  public final Field getGetterField(String name) {
    return getters.get(name);
  }

  public final List<Field> getFields() {
    return Collections.unmodifiableList(orderedFields);
  }

  public final Object getFieldValue(String name) {
    final Field field = fields.get(name);
    if (field != null) {
      return field.getValue();
    }
    throw new RosMessageRuntimeException("Unknown field: " + name);
  }

  public final void setFieldValue(final String name,final  Object value) {
    final Field field = fields.get(name);
    if (field != null) {
      field.setValue(value);
    } else {
      throw new RosMessageRuntimeException("Unknown field: " + name);
    }
  }

  @Override
  public final int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fields == null) ? 0 : fields.hashCode());
    result = prime * result + ((orderedFields == null) ? 0 : orderedFields.hashCode());
    return result;
  }

  @Override
  public final boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MessageFields other = (MessageFields) obj;
    if (fields == null) {
      if (other.fields != null)
        return false;
    } else if (!fields.equals(other.fields))
      return false;
    if (orderedFields == null) {
      if (other.orderedFields != null)
        return false;
    } else if (!orderedFields.equals(other.orderedFields))
      return false;
    return true;
  }
}
