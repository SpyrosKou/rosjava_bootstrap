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

import com.google.common.base.Preconditions;
import org.ros.internal.message.context.MessageContext;
import org.ros.internal.message.context.MessageContextProvider;
import org.ros.message.MessageDeclarationImpl;
import org.ros.message.MessageFactory;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * @author damonkohler@google.com (Damon Kohler)
 * @author Spyros Koukas
 */
public final class MessageProxyFactory {

    private static final AtomicInteger SEQUENCE_NUMBER = new AtomicInteger(0);

    private final MessageInterfaceClassProvider messageInterfaceClassProvider;
    private final MessageContextProvider messageContextProvider;

    public MessageProxyFactory(MessageInterfaceClassProvider messageInterfaceClassProvider,
                               MessageFactory messageFactory) {
        this.messageInterfaceClassProvider = messageInterfaceClassProvider;
        messageContextProvider = new MessageContextProvider(messageFactory);
    }

    @SuppressWarnings("unchecked")
    public final <T extends Message> T newMessageProxy(final MessageDeclarationImpl messageDeclaration) {
        Preconditions.checkNotNull(messageDeclaration);
        final MessageContext messageContext = messageContextProvider.get(messageDeclaration);
        final MessageImpl messageImpl = new MessageImpl(messageContext);
        // Header messages are automatically populated with a monotonically
        // increasing sequence number.
        if (messageImpl.getType().equals(MessageConstants.HEADER_MESSAGE_TYPE)) {
            messageImpl.setUInt32(MessageConstants.SEQUENCE_FIELD_NAME, SEQUENCE_NUMBER.getAndIncrement());
        }
        final Class<T> messageInterfaceClass = messageInterfaceClassProvider.get(messageDeclaration.getType());
        return newProxy(messageInterfaceClass, messageImpl);
    }

    /**
     * @param interfaceClass the interface class to provide
     * @param messageImpl    the instance to proxy
     *
     * @return a new proxy for {@code implementation} that implements
     * {@code interfaceClass}
     */
    @SuppressWarnings("unchecked")
    private final <T extends Message> T newProxy(Class<T> interfaceClass, final MessageImpl messageImpl) {
        final ClassLoader classLoader = messageImpl.getClass().getClassLoader();
        final Class<?>[] interfaces = new Class<?>[]{interfaceClass, Supplier.class};
        final MessageProxyInvocationHandler invocationHandler = new MessageProxyInvocationHandler(messageImpl);
        return (T) Proxy.newProxyInstance(classLoader, interfaces, invocationHandler);
    }
}
