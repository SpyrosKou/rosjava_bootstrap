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

package org.ros.internal.message.definition;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.ros.message.MessageDefinitionProvider;
import org.ros.message.MessageIdentifier;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageDefinitionProviderChain implements MessageDefinitionProvider {

  private final List<MessageDefinitionProvider> messageDefinitionProviders;

  public MessageDefinitionProviderChain() {
    messageDefinitionProviders = Lists.newArrayList();
  }

  public void addMessageDefinitionProvider(MessageDefinitionProvider messageDefinitionProvider) {
    messageDefinitionProviders.add(messageDefinitionProvider);
  }

  @Override
  public String get(String messageType) {
    for (MessageDefinitionProvider messageDefinitionProvider : messageDefinitionProviders) {
      if (messageDefinitionProvider.has(messageType)) {
        return messageDefinitionProvider.get(messageType);
      }
    }
    throw new NoSuchElementException("No message definition available for: " + messageType);
  }

  @Override
  public boolean has(String messageType) {
    for (MessageDefinitionProvider messageDefinitionProvider : messageDefinitionProviders) {
      if (messageDefinitionProvider.has(messageType)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Set<String> getPackages() {
    final Set<String> result = Sets.newHashSet();
    for (final MessageDefinitionProvider messageDefinitionProvider : messageDefinitionProviders) {
      Set<String> packages = messageDefinitionProvider.getPackages();
      result.addAll(packages);
    }
    return result;
  }

  @Override
  public Set<MessageIdentifier> getMessageIdentifiersByPackage(String pkg) {
    final Set<MessageIdentifier> result = Sets.newHashSet();
    for (final MessageDefinitionProvider messageDefinitionProvider : messageDefinitionProviders) {
      Collection<MessageIdentifier> messageIdentifiers =
          messageDefinitionProvider.getMessageIdentifiersByPackage(pkg);
      if (messageIdentifiers != null) {
        result.addAll(messageIdentifiers);
      }
    }
    return result;
  }
}
