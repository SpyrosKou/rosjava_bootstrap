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

import org.jboss.netty.buffer.ChannelBuffer;
import org.ros.exception.RosMessageRuntimeException;
import org.ros.internal.message.context.MessageContext;
import org.ros.internal.message.field.Field;
import org.ros.internal.message.field.MessageFieldType;
import org.ros.internal.message.field.MessageFields;
import org.ros.message.Duration;
import org.ros.message.MessageIdentifier;
import org.ros.message.Time;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author damonkohler@google.com (Damon Kohler)
 * @author Spyros Koukas
 */
final class MessageImpl implements RawMessage, Supplier<Object> {

  private final MessageContext messageContext;
  private final MessageFields messageFields;

  public MessageImpl(final MessageContext messageContext) {
    this.messageContext = messageContext;
    this.messageFields = new MessageFields(messageContext);
  }

  public final MessageContext getMessageContext() {
    return messageContext;
  }

  public final MessageFields getMessageFields() {
    return messageFields;
  }

  @Override
  public final RawMessage toRawMessage() {
    return this;
  }

  @Override
  public final MessageIdentifier getIdentifier() {
    return messageContext.getMessageIdentifer();
  }

  @Override
  public final String getType() {
    return messageContext.getType();
  }

  @Override
  public final String getPackage() {
    return messageContext.getPackage();
  }

  @Override
  public final String getName() {
    return messageContext.getName();
  }

  @Override
  public final String getDefinition() {
    return messageContext.getDefinition();
  }

  @Override
  public final List<Field> getFields() {
    return messageFields.getFields();
  }

  @Override
  public final boolean getBool(String name) {
    return (Boolean) messageFields.getFieldValue(name);
  }

  @Override
  public final boolean[] getBoolArray(String name) {
    return (boolean[]) messageFields.getFieldValue(name);
  }

  @Override
  public final Duration getDuration(String name) {
    return (Duration) messageFields.getFieldValue(name);
  }

  @SuppressWarnings("unchecked")
  @Override
  public final List<Duration> getDurationList(String name) {
    return (List<Duration>) messageFields.getFieldValue(name);
  }

  @Override
  public final float getFloat32(String name) {
    return (Float) messageFields.getFieldValue(name);
  }

  @Override
  public final float[] getFloat32Array(String name) {
    return (float[]) messageFields.getFieldValue(name);
  }

  @Override
  public final double getFloat64(String name) {
    return (Double) messageFields.getFieldValue(name);
  }

  @Override
  public final double[] getFloat64Array(String name) {
    return (double[]) messageFields.getFieldValue(name);
  }

  @Override
  public final short getInt16(String name) {
    return (Short) messageFields.getFieldValue(name);
  }

  @Override
  public final short[] getInt16Array(String name) {
    return (short[]) messageFields.getFieldValue(name);
  }

  @Override
  public final int getInt32(String name) {
    return (Integer) messageFields.getFieldValue(name);
  }

  @Override
  public final int[] getInt32Array(String name) {
    return (int[]) messageFields.getFieldValue(name);
  }

  @Override
  public final long getInt64(String name) {
    return (Long) messageFields.getFieldValue(name);
  }

  @Override
  public final long[] getInt64Array(String name) {
    return (long[]) messageFields.getFieldValue(name);
  }

  @Override
  public final byte getInt8(String name) {
    return (Byte) messageFields.getFieldValue(name);
  }

  @Override
  public final ChannelBuffer getInt8Array(String name) {
    return (ChannelBuffer) messageFields.getFieldValue(name);
  }

  @Override
  public final <T extends Message> T getMessage(String name) {
    if (messageFields.getField(name).getType() instanceof MessageFieldType) {
      return messageFields.getField(name).<T>getValue();
    }
    throw new RosMessageRuntimeException("Failed to access message field: " + name);
  }

  @Override
  public final <T extends Message> List<T> getMessageList(final String name) {
    if (messageFields.getField(name).getType() instanceof MessageFieldType) {
      return messageFields.getField(name).<List<T>>getValue();
    }
    throw new RosMessageRuntimeException("Failed to access list field: " + name);
  }

  @Override
  public final String getString(final String name) {
    return (String) messageFields.getFieldValue(name);
  }

  @SuppressWarnings("unchecked")
  @Override
  public final List<String> getStringList(final String name) {
    return (List<String>) messageFields.getFieldValue(name);
  }

  @Override
  public final Time getTime(final String name) {
    return (Time) messageFields.getFieldValue(name);
  }

  @SuppressWarnings("unchecked")
  @Override
  public final List<Time> getTimeList(final String name) {
    return (List<Time>) messageFields.getFieldValue(name);
  }

  @Override
  public final short getUInt16(String name) {
    return (Short) messageFields.getFieldValue(name);
  }

  @Override
  public final short[] getUInt16Array(String name) {
    return (short[]) messageFields.getFieldValue(name);
  }

  @Override
  public final int getUInt32(String name) {
    return (Integer) messageFields.getFieldValue(name);
  }

  @Override
  public final int[] getUInt32Array(String name) {
    return (int[]) messageFields.getFieldValue(name);
  }

  @Override
  public final long getUInt64(String name) {
    return (Long) messageFields.getFieldValue(name);
  }

  @Override
  public final long[] getUInt64Array(String name) {
    return (long[]) messageFields.getFieldValue(name);
  }

  @Override
  public final short getUInt8(String name) {
    return (Short) messageFields.getFieldValue(name);
  }

  @Override
  public final short[] getUInt8Array(String name) {
    return (short[]) messageFields.getFieldValue(name);
  }

  @Override
  public final void setBool(String name, boolean value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setBoolArray(String name, boolean[] value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setDurationList(String name, List<Duration> value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setDuration(String name, Duration value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setFloat32(String name, float value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setFloat32Array(String name, float[] value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setFloat64(String name, double value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setFloat64Array(String name, double[] value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setInt16(String name, short value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setInt16Array(String name, short[] value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setInt32(String name, int value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setInt32Array(String name, int[] value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setInt64(String name, long value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setInt64Array(String name, long[] value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setInt8(String name, byte value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setInt8Array(String name, byte[] value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setMessage(String name, Message value) {
    // TODO(damonkohler): Verify the type of the provided Message?
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setMessageList(String name, List<Message> value) {
    // TODO(damonkohler): Verify the type of all Messages in the provided list?
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setString(String name, String value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setStringList(String name, List<String> value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setTime(String name, Time value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setTimeList(String name, List<Time> value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setUInt16(String name, short value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setUInt16Array(String name, short[] value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setUInt32(String name, int value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setUInt32Array(String name, int[] value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setUInt64(String name, long value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setUInt64Array(String name, long[] value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setUInt8(String name, byte value) {
    messageFields.setFieldValue(name, value);
  }

  @Override
  public final void setUInt8Array(String name, byte[] value) {
    messageFields.setFieldValue(name, value);
  }


  @Override
  public final ChannelBuffer getChannelBuffer(String name) {
    return (ChannelBuffer) messageFields.getFieldValue(name);
  }

  @Override
  public final void setChannelBuffer(String name, ChannelBuffer value) {
    messageFields.setFieldValue(name, value);
  }
  
  @Override
  public final Object get() {
    return this;
  }

  @Override
  public final String toString() {
    return String.format("MessageImpl<%s>", getType());
  }

  @Override
  public final int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((messageContext == null) ? 0 : messageContext.hashCode());
    result = prime * result + ((messageFields == null) ? 0 : messageFields.hashCode());
    return result;
  }

  @Override
  public final boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof Supplier<?>))
      return false;
    obj = ((Supplier<Object>) obj).get();
    if (getClass() != obj.getClass())
      return false;
    MessageImpl other = (MessageImpl) obj;
    if (messageContext == null) {
      if (other.messageContext != null)
        return false;
    } else if (!messageContext.equals(other.messageContext))
      return false;
    if (messageFields == null) {
      if (other.messageFields != null)
        return false;
    } else if (!messageFields.equals(other.messageFields))
      return false;
    return true;
  }
}
