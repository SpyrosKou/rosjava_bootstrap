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

package org.ros2.internal.message.service;

import com.google.common.base.Preconditions;
import org.ros2.internal.message.DefaultMessageFactory;
import org.ros2.internal.message.DefaultMessageInterfaceClassProvider;
import org.ros2.internal.message.Message;
import org.ros2.internal.message.MessageProxyFactory;
import org.ros2.message.MessageDeclarationImpl;
import org.ros2.message.MessageDefinitionProvider;
import org.ros2.message.MessageFactory;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public final class ServiceRequestMessageFactory implements MessageFactory {

  private final ServiceDescriptionFactory serviceDescriptionFactory;
  private final MessageFactory messageFactory;
  private final MessageProxyFactory messageProxyFactory;

  public ServiceRequestMessageFactory(final MessageDefinitionProvider messageDefinitionProvider) {
    Preconditions.checkNotNull(messageDefinitionProvider);
    serviceDescriptionFactory = new ServiceDescriptionFactory(messageDefinitionProvider);
    messageFactory = new DefaultMessageFactory(messageDefinitionProvider);
    messageProxyFactory =
        new MessageProxyFactory(new DefaultMessageInterfaceClassProvider(), messageFactory);
  }

  @Override
  public final <T extends Message> T newFromType(String serviceType) {
    ServiceDescription serviceDescription = serviceDescriptionFactory.newFromType(serviceType);
    MessageDeclarationImpl messageDeclaration =
        MessageDeclarationImpl.of(serviceDescription.getRequestType(),
            serviceDescription.getRequestDefinition());
    return messageProxyFactory.newMessageProxy(messageDeclaration);
  }
}
