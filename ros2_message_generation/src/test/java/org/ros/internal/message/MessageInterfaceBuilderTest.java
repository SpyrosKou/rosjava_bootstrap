/*
 * Copyright (C) 2025 Spyros Koukas
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

package org.ros.internal.message;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ros.internal.message.topic.TopicDefinitionResourceProvider;
import org.ros.message.MessageDeclarationImpl;
import org.ros.message.MessageFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author damonkohler@google.com (Damon Kohler)
 * @author https://github.com/SpyrosKou/ Spyros Koukas
 */
public class MessageInterfaceBuilderTest {

  private TopicDefinitionResourceProvider topicDefinitionResourceProvider;
  private MessageFactory messageFactory;

  @BeforeEach
  public void before() {
    topicDefinitionResourceProvider = new TopicDefinitionResourceProvider();
    messageFactory = new DefaultMessageFactory(topicDefinitionResourceProvider);
  }

  /**
   *  Field names with different caps are allowed but should throw a warning.
   */
  @Test
  public void testDuplicateFieldNames() {
    MessageInterfaceBuilder builder = new MessageInterfaceBuilder();
    builder.setPackageName("foo");
    builder.setInterfaceName("bar");
    builder.setMessageDeclaration(MessageDeclarationImpl.of("foo/bar", "int32 foo\nint32 Foo"));
    builder.setAddConstantsAndMethods(true);
    String result = builder.build(messageFactory);
    Assertions.assertTrue(result.contains("int Foo"));
    Assertions.assertTrue(result.contains("int foo"));
  }
}
