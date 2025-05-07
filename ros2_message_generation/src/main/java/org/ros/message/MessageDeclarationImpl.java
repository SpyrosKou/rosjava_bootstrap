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

package org.ros.message;

import com.google.common.base.Preconditions;

/**
 * An {@link MessageIdentifierImpl} and definition pair from which all qualities of
 * the message uniquely identifiable by the {@link MessageIdentifierImpl} can be
 * derived.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageDeclarationImpl implements MessageDeclaration {

  private final MessageIdentifier messageIdentifier;
  private final String definition;

  public static final MessageDeclarationImpl of(String type, String definition) {
    Preconditions.checkNotNull(type);
    Preconditions.checkNotNull(definition);
    return new MessageDeclarationImpl(MessageIdentifierImpl.of(type), definition);
  }

  /**
   * @param messageIdentifier
   *          the {@link MessageIdentifierImpl}
   * @param definition
   *          the message definition
   */
  public MessageDeclarationImpl(MessageIdentifier messageIdentifier, String definition) {
    Preconditions.checkNotNull(messageIdentifier);
    Preconditions.checkNotNull(definition);
    this.messageIdentifier = messageIdentifier;
    this.definition = definition;
  }

  @Override
  public MessageIdentifier getMessageIdentifier() {
    return messageIdentifier;
  }

  @Override
  public final  String getType() {
    return messageIdentifier.getType();
  }

  @Override
  public final  String getPackage() {
    return messageIdentifier.getPackage();
  }

  @Override
  public final String getName() {
    return messageIdentifier.getName();
  }

  @Override
  public final String getDefinition() {
    Preconditions.checkNotNull(definition);
    return definition;
  }

  @Override
  public String toString() {
    return String.format("MessageDeclaration<%s>", messageIdentifier.toString());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((definition == null) ? 0 : definition.hashCode());
    result = prime * result + ((messageIdentifier == null) ? 0 : messageIdentifier.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MessageDeclarationImpl other = (MessageDeclarationImpl) obj;
    if (definition == null) {
      if (other.definition != null)
        return false;
    } else if (!definition.equals(other.definition))
      return false;
    if (messageIdentifier == null) {
      if (other.messageIdentifier != null)
        return false;
    } else if (!messageIdentifier.equals(other.messageIdentifier))
      return false;
    return true;
  }
}