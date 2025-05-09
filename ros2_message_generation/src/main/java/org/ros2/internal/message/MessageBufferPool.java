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

package org.ros2.internal.message;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.StackObjectPool;
import org.jboss.netty.buffer.ChannelBuffer;
import org.ros2.exception.RosMessageRuntimeException;

/**
 * A pool of {@link ChannelBuffer}s for serializing and deserializing messages.
 * <p>
 * By contract, {@link ChannelBuffer}s provided by {@link #acquire()} must be
 * returned using {@link #release(ChannelBuffer)}.
 *
 * @author damonkohler@google.com (Damon Kohler)
 */
public final class MessageBufferPool {

    private final ObjectPool<ChannelBuffer> pool;

    public MessageBufferPool() {
        this.pool = new StackObjectPool<>(new PoolableObjectFactory<>() {
            @Override
            public final ChannelBuffer makeObject() {
                return MessageBuffers.dynamicBuffer();
            }

            @Override
            public final void destroyObject(final ChannelBuffer channelBuffer) {

            }

            @Override
            public final boolean validateObject(final ChannelBuffer channelBuffer) {
                return true;
            }

            @Override
            public final void activateObject(final ChannelBuffer channelBuffer) {
            }

            @Override
            public final void passivateObject(final ChannelBuffer channelBuffer) {
                channelBuffer.clear();
            }
        });
    }

    /**
     * Acquired {@link ChannelBuffer}s must be returned using
     * {@link #release(ChannelBuffer)}.
     *
     * @return an unused {@link ChannelBuffer}
     */
    public final ChannelBuffer acquire() {
        try {
            return this.pool.borrowObject();
        } catch (Exception exception) {
            throw new RosMessageRuntimeException(exception);
        }
    }

    /**
     * Release a previously acquired {@link ChannelBuffer}.
     *
     * @param channelBuffer the {@link ChannelBuffer} to release
     */
    public void release(final ChannelBuffer channelBuffer) {
        try {
            this.pool.returnObject(channelBuffer);
        } catch (final Exception exception) {
            throw new RosMessageRuntimeException(exception);
        }
    }
}
