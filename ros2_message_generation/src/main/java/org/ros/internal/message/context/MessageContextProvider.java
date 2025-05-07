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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.ros.internal.message.definition.MessageDefinitionParser;
import org.ros.internal.message.definition.MessageDefinitionParser.MessageDefinitionVisitor;
import org.ros.message.MessageDeclarationImpl;
import org.ros.message.MessageFactory;

import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public final class MessageContextProvider {

    private final Map<MessageDeclarationImpl, MessageContext> cache = Maps.newConcurrentMap();
    private final MessageFactory messageFactory;

    public MessageContextProvider(final MessageFactory messageFactory) {
        Preconditions.checkNotNull(messageFactory);
        this.messageFactory = messageFactory;
    }

    private final MessageContext createMessageContext(final MessageDeclarationImpl messageDeclarationImpl) {
        final MessageContext messageContext = new MessageContext(messageDeclarationImpl, this.messageFactory);
        final MessageDefinitionVisitor visitor = new MessageContextBuilder(messageContext);
        final MessageDefinitionParser messageDefinitionParser = new MessageDefinitionParser(visitor);
        messageDefinitionParser.parse(messageDeclarationImpl.getType(), messageDeclarationImpl.getDefinition());
        return messageContext;
    }

    public final MessageContext get(final MessageDeclarationImpl messageDeclaration) {
        final MessageContext messageContext = this.cache.computeIfAbsent(messageDeclaration, this::createMessageContext);

        return messageContext;
    }
}
