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

package org.ros2.internal.message.field;

import com.google.common.base.Preconditions;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.ros2.internal.message.MessageBuffers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.nio.ByteOrder;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
final class ChannelBufferField extends Field {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String ORG_JBOSS_NETTY_BUFFER_CHANNEL_BUFFER = "org.jboss.netty.buffer.ChannelBuffer";
    private final int size;

    private ChannelBuffer value;

    public static final ChannelBufferField newVariable(final FieldType type,final String name,final int size) {
        return new ChannelBufferField(type, name, size);
    }

    private ChannelBufferField(final FieldType type, final String name, final int size) {
        super(type, name, false);
        this.size = size;
        this.value = MessageBuffers.dynamicBuffer();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final ChannelBuffer getValue() {
        // Return a defensive duplicate. Unlike with copy(), duplicated
        // ChannelBuffers share the same backing array, so this is relatively cheap.
        return value.duplicate();
    }

    @Override
    public final void setValue(final Object value) {
        final ChannelBuffer channelBufferValue;
        if (value instanceof byte[]) {
            channelBufferValue = ChannelBuffers.wrappedBuffer(ByteOrder.LITTLE_ENDIAN, byte[].class.cast(value));
        } else if (value instanceof ChannelBuffer) {
            channelBufferValue = ChannelBuffer.class.cast(value);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Null value for value:" + value + " type:" + this.getJavaTypeName());
            }
            channelBufferValue = null;
        }
        Preconditions.checkArgument(channelBufferValue.order() == ByteOrder.LITTLE_ENDIAN);
        Preconditions.checkArgument(size < 0 || channelBufferValue.readableBytes() == size);
        this.value = channelBufferValue;
    }

    @Override
    public final void serialize(ChannelBuffer buffer) {
        if (size < 0) {
            buffer.writeInt(value.readableBytes());
        }
        // By specifying the start index and length we avoid modifying value's
        // indices and marks.
        buffer.writeBytes(value, 0, value.readableBytes());
    }

    @Override
    public final void deserialize(ChannelBuffer buffer) {
        int currentSize = size;
        if (currentSize < 0) {
            currentSize = buffer.readInt();
        }
        value = buffer.readSlice(currentSize);
    }

    @Override
    public final String getMd5String() {
        return String.format("%s %s\n", type, name);
    }

    @Override
    public final String getJavaTypeName() {
        return ORG_JBOSS_NETTY_BUFFER_CHANNEL_BUFFER;
    }

    @Override
    public final String toString() {
        return "ChannelBufferField<" + type + ", " + name + ">";
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ChannelBufferField other = (ChannelBufferField) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
}
