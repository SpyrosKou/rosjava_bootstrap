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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.commons.io.FileUtils;
import org.ros.exception.RosMessageRuntimeException;
import org.ros.internal.message.definition.MessageDefinitionProviderChain;
import org.ros.internal.message.definition.MessageDefinitionTupleParser;
import org.ros.internal.message.action.ActionDefinitionFileProvider;
import org.ros.internal.message.action.ActionGenerationTemplateActionGoal;
import org.ros.internal.message.action.ActionGenerationTemplateActionResult;
import org.ros.internal.message.action.ActionGenerationTemplateActionFeedback;
import org.ros.internal.message.action.ActionGenerationTemplateGoal;
import org.ros.internal.message.action.ActionGenerationTemplateResult;
import org.ros.internal.message.action.ActionGenerationTemplateFeedback;
import org.ros.internal.message.service.ServiceDefinitionFileProvider;
import org.ros.internal.message.topic.TopicDefinitionFileProvider;
import org.ros.message.MessageDeclaration;
import org.ros.message.MessageFactory;
import org.ros.message.MessageIdentifier;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * @author damonkohler@google.com (Damon Kohler)
 * @author Spyros Koukas
 */
public final class GenerateInterfaces {

  private final TopicDefinitionFileProvider topicDefinitionFileProvider= new TopicDefinitionFileProvider();
  private final ServiceDefinitionFileProvider serviceDefinitionFileProvider= new ServiceDefinitionFileProvider();
  private final MessageDefinitionProviderChain messageDefinitionProviderChain=new MessageDefinitionProviderChain();
  private final ActionDefinitionFileProvider actionDefinitionFileProvider = new ActionDefinitionFileProvider();

  private final MessageFactory messageFactory;

  private final MessageGenerationTemplate actionGenerationTemplateGoal     = new ActionGenerationTemplateGoal();
  private final MessageGenerationTemplate actionGenerationTemplateResult   = new ActionGenerationTemplateResult();
  private final MessageGenerationTemplate actionGenerationTemplateFeedback = new ActionGenerationTemplateFeedback();

  private final MessageGenerationTemplate actionGenerationTemplateActionGoal     = new ActionGenerationTemplateActionGoal();
  private final MessageGenerationTemplate actionGenerationTemplateActionResult   = new ActionGenerationTemplateActionResult();
  private final MessageGenerationTemplate actionGenerationTemplateActionFeedback = new ActionGenerationTemplateActionFeedback();

  static private final String ROS_PACKAGE_PATH = "ROS_PACKAGE_PATH";

  public GenerateInterfaces() {
    this.messageDefinitionProviderChain.addMessageDefinitionProvider(topicDefinitionFileProvider);
    this.messageDefinitionProviderChain.addMessageDefinitionProvider(serviceDefinitionFileProvider);
    this.messageDefinitionProviderChain.addMessageDefinitionProvider(actionDefinitionFileProvider);
    this.messageFactory = new DefaultMessageFactory(messageDefinitionProviderChain);
  }

  /**
   * @param packages
   *          a list of packages containing the topic types to generate
   *          interfaces for
   * @param outputDirectory
   *          the directory to write the generated interfaces to
   * @throws IOException
   */
  private void writeTopicInterfaces(File outputDirectory, Collection<String> packages)
      throws IOException {
    Set<MessageIdentifier> topicTypes = Sets.newHashSet();
    if (packages.size() == 0) {
      packages = topicDefinitionFileProvider.getPackages();
    }
    for (final String pkg : packages) {
      final Set<MessageIdentifier> messageIdentifiers =
          topicDefinitionFileProvider.getMessageIdentifiersByPackage(pkg);
      if (messageIdentifiers != null) {
        topicTypes.addAll(messageIdentifiers);
      }
    }
    for (MessageIdentifier topicType : topicTypes) {
      String definition = messageDefinitionProviderChain.get(topicType.getType());
      MessageDeclaration messageDeclaration = new MessageDeclaration(topicType, definition);
      writeInterface(messageDeclaration, outputDirectory, true);
    }
  }

  /**
   * @param packages
   *          a list of packages containing the topic types to generate
   *          interfaces for
   * @param outputDirectory
   *          the directory to write the generated interfaces to
   * @throws IOException
   */
  private void writeServiceInterfaces(File outputDirectory, Collection<String> packages)
      throws IOException {
    final Set<MessageIdentifier> serviceTypes = Sets.newHashSet();
    if (packages.size() == 0) {
      packages = serviceDefinitionFileProvider.getPackages();
    }
    for (String pkg : packages) {
      Set<MessageIdentifier> messageIdentifiers =
          serviceDefinitionFileProvider.getMessageIdentifiersByPackage(pkg);
      if (messageIdentifiers != null) {
        serviceTypes.addAll(messageIdentifiers);
      }
    }
    for (MessageIdentifier serviceType : serviceTypes) {
      String definition = messageDefinitionProviderChain.get(serviceType.getType());
      MessageDeclaration serviceDeclaration =
          MessageDeclaration.of(serviceType.getType(), definition);
      writeInterface(serviceDeclaration, outputDirectory, false);
      List<String> requestAndResponse = MessageDefinitionTupleParser.parse(definition, 2);

      MessageDeclaration requestDeclaration =
          MessageDeclaration.of(serviceType.getType() + "Request", requestAndResponse.get(0));
      MessageDeclaration responseDeclaration =
          MessageDeclaration.of(serviceType.getType() + "Response", requestAndResponse.get(1));

      writeInterface(requestDeclaration, outputDirectory, true);
      writeInterface(responseDeclaration, outputDirectory, true);
    }
  }

  /**
   * @param packages
   *          a list of packages containing the topic types to generate
   *          interfaces for
   * @param outputDirectory
   *          the directory to write the generated interfaces to
   * @throws IOException
   */
  private void writeActionInterfaces(File outputDirectory, Collection<String> packages)
          throws IOException {
    final Set<MessageIdentifier> actionTypes = Sets.newHashSet();
    if (packages.size() == 0) {
      packages = actionDefinitionFileProvider.getPackages();
    }
    for (String pkg : packages) {
      final Set<MessageIdentifier> messageIdentifiers =
              actionDefinitionFileProvider.getMessageIdentifiersByPackage(pkg);
      if (messageIdentifiers != null) {
        actionTypes.addAll(messageIdentifiers);
      }
    }
    for (MessageIdentifier actionType : actionTypes) {
      String definition = messageDefinitionProviderChain.get(actionType.getType());
      MessageDeclaration actionDeclaration =
              MessageDeclaration.of(actionType.getType(), definition);
      writeInterface(actionDeclaration, outputDirectory, false);
      List<String> goalResultAndFeedback = MessageDefinitionTupleParser.parse(definition, 3);

      MessageDeclaration goalDeclaration = MessageDeclaration.of(
              actionType.getType() + "Goal",
              actionGenerationTemplateGoal.applyTemplate(goalResultAndFeedback.get(0))
      );
      MessageDeclaration resultDeclaration = MessageDeclaration.of(
              actionType.getType() + "Result",
              actionGenerationTemplateResult.applyTemplate(goalResultAndFeedback.get(1))
      );
      MessageDeclaration feedbackDeclaration = MessageDeclaration.of(
              actionType.getType() + "Feedback",
              actionGenerationTemplateFeedback.applyTemplate(goalResultAndFeedback.get(2))
      );

      MessageDeclaration actionGoalDeclaration = MessageDeclaration.of(
              actionType.getType() + "ActionGoal",
              actionGenerationTemplateActionGoal.applyTemplate(actionType.getType())
      );
      MessageDeclaration actionResultDeclaration = MessageDeclaration.of(
              actionType.getType() + "ActionResult",
              actionGenerationTemplateActionResult.applyTemplate(actionType.getType())
      );
      MessageDeclaration actionFeedbackDeclaration = MessageDeclaration.of(
              actionType.getType() + "ActionFeedback",
              actionGenerationTemplateActionFeedback.applyTemplate(actionType.getType())
      );

      writeInterface(goalDeclaration, outputDirectory, true);
      writeInterface(resultDeclaration, outputDirectory, true);
      writeInterface(feedbackDeclaration, outputDirectory, true);

      writeInterface(actionGoalDeclaration, outputDirectory, true);
      writeInterface(actionResultDeclaration, outputDirectory, true);
      writeInterface(actionFeedbackDeclaration, outputDirectory, true);
    }
  }

  private void writeInterface(MessageDeclaration messageDeclaration, File outputDirectory,
      boolean addConstantsAndMethods) {
    MessageInterfaceBuilder builder = new MessageInterfaceBuilder();
    builder.setPackageName(messageDeclaration.getPackage());
    builder.setInterfaceName(messageDeclaration.getName());
    builder.setMessageDeclaration(messageDeclaration);
    builder.setAddConstantsAndMethods(addConstantsAndMethods);
    try {
      String content;
      content = builder.build(messageFactory);
      File file = new File(outputDirectory, messageDeclaration.getType() + ".java");
      FileUtils.writeStringToFile(file, content);
    } catch (Exception e) {
      System.out.printf("Failed to generate interface for %s.\n", messageDeclaration.getType());
      e.printStackTrace();
    }
  }

  /**
   *
   * @param outputDirectory
   * @param packages
   * @param packagePath
   */
  public final void generate(
          final File outputDirectory
          ,final Collection<String> packages
          ,final Collection<File> packagePath) {
    for (final File directory : packagePath) {
      this.topicDefinitionFileProvider.addDirectory(directory);
      this.serviceDefinitionFileProvider.addDirectory(directory);
      this.actionDefinitionFileProvider.addDirectory(directory);
    }
    this.topicDefinitionFileProvider.update();
    this.serviceDefinitionFileProvider.update();
    this.actionDefinitionFileProvider.update();
    try {
      writeTopicInterfaces(outputDirectory, packages);
      writeServiceInterfaces(outputDirectory, packages);
      writeActionInterfaces(outputDirectory, packages);
    } catch (final IOException ioException) {
      throw new RosMessageRuntimeException(ioException);
    }
  }

  public static void main(String[] args) {
    final List<String> arguments = Lists.newArrayList(args);
    if (arguments.size() == 0) {
      arguments.add(".");
    }

    String rosPackagePath = System.getenv(ROS_PACKAGE_PATH);
    // Overwrite with a supplied package path if specified (--package-path=)
    for (ListIterator<String> iter = arguments.listIterator(); iter.hasNext(); ) {
      String arg = iter.next();
      if (arg.contains("--package-path=")) {
        rosPackagePath = arg.replace("--package-path=", "");
        iter.remove();
        break;
      }
    }

    final List<File> packagePath = Lists.newArrayList();
    for (final String path : rosPackagePath.split(File.pathSeparator)) {
      final File packageDirectory = new File(path);
      if (packageDirectory.exists()) {
        packagePath.add(packageDirectory);
      }
    }

    final GenerateInterfaces generateInterfaces = new GenerateInterfaces();
    final File outputDirectory = new File(arguments.remove(0));
    generateInterfaces.generate(outputDirectory, arguments, packagePath);
  }
}
