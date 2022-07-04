/*
 * Copyright (C) 2014 Google Inc.
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.ros.exception.RosMessageRuntimeException;
import org.ros.internal.message.definition.MessageDefinitionReflectionProvider;
import org.ros.internal.message.definition.MessageDefinitionTupleParser;
import org.ros.message.MessageDeclaration;
import org.ros.message.MessageFactory;
import org.ros.message.MessageIdentifier;

import com.google.common.collect.Lists;

/**
 * @author d.stonier@gmail.com (Daniel Stonier)
 */
public final class GenerateInterface {

  private static final void writeInterface(final MessageDeclaration messageDeclaration, final File outputDirectory,
                                     final boolean addConstantsAndMethods,final MessageFactory messageFactory) {
    final MessageInterfaceBuilder builder = new MessageInterfaceBuilder();
    builder.setPackageName(messageDeclaration.getPackage());
    builder.setInterfaceName(messageDeclaration.getName());
    builder.setMessageDeclaration(messageDeclaration);
    builder.setAddConstantsAndMethods(addConstantsAndMethods);
    try {
      final String content = builder.build(messageFactory);
      final File file = new File(outputDirectory, messageDeclaration.getType() + MessageConstants.JAVA);
      System.out.println("Output File: " + file.getAbsolutePath());
      FileUtils.writeStringToFile(file, content, Charset.defaultCharset());
    } catch (Exception e) {
      System.out.printf("Failed to generate interface for %s.\n", messageDeclaration.getType());
      e.printStackTrace();
    }
  }

  public static final void main(String[] args) {
    List<String> arguments = Lists.newArrayList(args);
    if (arguments.size() != 3) {
      System.out
          .println("Incorrect usage, please provide two args: _output_directory_, _pkg_ and _path_to_msg/srv_file_");
      System.exit(1);
    }
    final File outputDirectory = new File(arguments.remove(0));
    final String pkg = arguments.remove(0);
    final File file = new File(arguments.remove(0));

    System.out.println("Output Directory: " + outputDirectory.getAbsolutePath());
    System.out.println("Package: " + pkg);
    System.out.println("Message: " + file.getAbsolutePath());

    final String name = FilenameUtils.getBaseName(file.getName());
    final String extension = FilenameUtils.getExtension(file.getName());

    System.out.println("  Name: " + name);
    System.out.println("  Extension: " + extension);
    final String definition;
    try {
      definition = FileUtils.readFileToString(file, "US-ASCII");
    } catch (IOException e) {
      throw new RosMessageRuntimeException(e);
    }
    final MessageIdentifier messageIdentifier = MessageIdentifier.of(pkg, name);
    final MessageDeclaration messageDeclaration = new MessageDeclaration(messageIdentifier, definition);
    final MessageDefinitionReflectionProvider messageDefinitionProvider =
        new MessageDefinitionReflectionProvider();
    messageDefinitionProvider.add(messageIdentifier.getType(), definition);
    final MessageFactory messageFactory = new DefaultMessageFactory(messageDefinitionProvider);
    if (extension.equals(MessageConstants.MSG)) {
      writeInterface(messageDeclaration, outputDirectory, true, messageFactory);
    } else if (extension.equals(MessageConstants.SRV)) {
      writeInterface(messageDeclaration, outputDirectory, false, messageFactory);
      List<String> requestAndResponse = MessageDefinitionTupleParser.parse(definition, 2);
      final MessageDeclaration requestDeclaration =
          MessageDeclaration.of(messageIdentifier.getType() + MessageConstants.REQUEST, requestAndResponse.get(0));
      final MessageDeclaration responseDeclaration =
          MessageDeclaration
              .of(messageIdentifier.getType() + MessageConstants.RESPONSE, requestAndResponse.get(1));
      writeInterface(requestDeclaration, outputDirectory, true, messageFactory);
      writeInterface(responseDeclaration, outputDirectory, true, messageFactory);
    }
  }
}
