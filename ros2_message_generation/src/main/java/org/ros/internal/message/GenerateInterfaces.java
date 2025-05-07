/*
 * Copyright (C) 2025 Spyros Koukas.
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
import org.ros.internal.message.action.*;
import org.ros.internal.message.definition.MessageDefinitionProviderChain;
import org.ros.internal.message.definition.MessageDefinitionTupleParser;
import org.ros.internal.message.service.ServiceDefinitionFileProvider;
import org.ros.internal.message.topic.TopicDefinitionFileProvider;
import org.ros.message.MessageDeclarationImpl;
import org.ros.message.MessageFactory;
import org.ros.message.MessageIdentifier;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author damonkohler@google.com (Damon Kohler)
 * @author https://github.com/SpyrosKou/ Spyros Koukas
 */
public final class GenerateInterfaces {

    private final TopicDefinitionFileProvider topicDefinitionFileProvider = new TopicDefinitionFileProvider();
    private final ServiceDefinitionFileProvider serviceDefinitionFileProvider = new ServiceDefinitionFileProvider();
    private final MessageDefinitionProviderChain messageDefinitionProviderChain = new MessageDefinitionProviderChain();
    private final ActionDefinitionFileProvider actionDefinitionFileProvider = new ActionDefinitionFileProvider();

    private final MessageFactory messageFactory;

    private int successfulInterfaceGenerations = 0;
    private int failedInterfaceGenerations = 0;

    private final MessageGenerationTemplate actionGenerationTemplateGoal = new ActionGenerationTemplateGoal();
    private final MessageGenerationTemplate actionGenerationTemplateResult = new ActionGenerationTemplateResult();
    private final MessageGenerationTemplate actionGenerationTemplateFeedback = new ActionGenerationTemplateFeedback();

    private final MessageGenerationTemplate actionGenerationTemplateActionGoal = new ActionGenerationTemplateActionGoal();
    private final MessageGenerationTemplate actionGenerationTemplateActionResult = new ActionGenerationTemplateActionResult();
    private final MessageGenerationTemplate actionGenerationTemplateActionFeedback = new ActionGenerationTemplateActionFeedback();



    public GenerateInterfaces() {
        this.messageDefinitionProviderChain.addMessageDefinitionProvider(topicDefinitionFileProvider);
        this.messageDefinitionProviderChain.addMessageDefinitionProvider(serviceDefinitionFileProvider);
        this.messageDefinitionProviderChain.addMessageDefinitionProvider(actionDefinitionFileProvider);
        this.messageFactory = new DefaultMessageFactory(messageDefinitionProviderChain);
    }

    /**
     * @param packages        a list of packages containing the topic types to generate
     *                        interfaces for
     * @param outputDirectory the directory to write the generated interfaces to
     * @throws IOException
     */
    private void writeTopicInterfaces(File outputDirectory, final Collection<String> packages) throws IOException {
        final Set<MessageIdentifier> topicTypes = new HashSet<>();
        final Set<String> actualPackages = new HashSet<>(packages);
        if (actualPackages.isEmpty()) {
            actualPackages.addAll(topicDefinitionFileProvider.getPackages());
        }
        for (final String pkg : actualPackages) {
            final Set<MessageIdentifier> messageIdentifiers =
                    topicDefinitionFileProvider.getMessageIdentifiersByPackage(pkg);
            if (messageIdentifiers != null) {
                topicTypes.addAll(messageIdentifiers);
            }
        }
        for (final MessageIdentifier topicType : topicTypes) {
            final String definition = this.messageDefinitionProviderChain.get(topicType.getType());
            final MessageDeclarationImpl messageDeclaration = new MessageDeclarationImpl(topicType, definition);
            this.writeInterface(messageDeclaration, outputDirectory, true);
        }
    }

    /**
     * @param packages        a list of packages containing the topic types to generate
     *                        interfaces for
     * @param outputDirectory the directory to write the generated interfaces to
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
            MessageDeclarationImpl serviceDeclaration =
                    MessageDeclarationImpl.of(serviceType.getType(), definition);
            this.writeInterface(serviceDeclaration, outputDirectory, false);
            List<String> requestAndResponse = MessageDefinitionTupleParser.parse(definition, 2);

            MessageDeclarationImpl requestDeclaration =
                    MessageDeclarationImpl.of(serviceType.getType() + "Request", requestAndResponse.get(0));
            MessageDeclarationImpl responseDeclaration =
                    MessageDeclarationImpl.of(serviceType.getType() + "Response", requestAndResponse.get(1));

            this.writeInterface(requestDeclaration, outputDirectory, true);
            this.writeInterface(responseDeclaration, outputDirectory, true);
        }
    }

    /**
     * @param packages        a list of packages containing the topic types to generate
     *                        interfaces for
     * @param outputDirectory the directory to write the generated interfaces to
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
        for (final MessageIdentifier actionType : actionTypes) {
            final String definition = messageDefinitionProviderChain.get(actionType.getType());
            final MessageDeclarationImpl actionDeclaration =
                    MessageDeclarationImpl.of(actionType.getType(), definition);
            this.writeInterface(actionDeclaration, outputDirectory, false);
            final List<String> goalResultAndFeedback = MessageDefinitionTupleParser.parse(definition, 3);

            final MessageDeclarationImpl goalDeclaration = MessageDeclarationImpl.of(
                    actionType.getType() + "Goal",
                    actionGenerationTemplateGoal.applyTemplate(goalResultAndFeedback.get(0))
            );
            final MessageDeclarationImpl resultDeclaration = MessageDeclarationImpl.of(
                    actionType.getType() + "Result",
                    actionGenerationTemplateResult.applyTemplate(goalResultAndFeedback.get(1))
            );
            final MessageDeclarationImpl feedbackDeclaration = MessageDeclarationImpl.of(
                    actionType.getType() + "Feedback",
                    actionGenerationTemplateFeedback.applyTemplate(goalResultAndFeedback.get(2))
            );

            final MessageDeclarationImpl actionGoalDeclaration = MessageDeclarationImpl.of(
                    actionType.getType() + "ActionGoal",
                    actionGenerationTemplateActionGoal.applyTemplate(actionType.getType())
            );
            final MessageDeclarationImpl actionResultDeclaration = MessageDeclarationImpl.of(
                    actionType.getType() + "ActionResult",
                    actionGenerationTemplateActionResult.applyTemplate(actionType.getType())
            );
            final MessageDeclarationImpl actionFeedbackDeclaration = MessageDeclarationImpl.of(
                    actionType.getType() + "ActionFeedback",
                    actionGenerationTemplateActionFeedback.applyTemplate(actionType.getType())
            );

            this.writeInterface(goalDeclaration, outputDirectory, true);
            this.writeInterface(resultDeclaration, outputDirectory, true);
            this.writeInterface(feedbackDeclaration, outputDirectory, true);

            this.writeInterface(actionGoalDeclaration, outputDirectory, true);
            this.writeInterface(actionResultDeclaration, outputDirectory, true);
            this.writeInterface(actionFeedbackDeclaration, outputDirectory, true);
        }
    }

    private final void writeInterface(MessageDeclarationImpl messageDeclaration, File outputDirectory,
                                      boolean addConstantsAndMethods) {
        final MessageInterfaceBuilder builder = new MessageInterfaceBuilder();
        builder.setPackageName(messageDeclaration.getPackage());
        builder.setInterfaceName(messageDeclaration.getName());
        builder.setMessageDeclaration(messageDeclaration);
        builder.setAddConstantsAndMethods(addConstantsAndMethods);
        try {
            final String content = builder.build(this.messageFactory);
            final File file = new File(outputDirectory, messageDeclaration.getType() + ".java");
            FileUtils.writeStringToFile(file, content, Charset.defaultCharset());
            this.successfulInterfaceGenerations++;
        } catch (Exception e) {
            this.failedInterfaceGenerations++;
            System.out.printf("Failed to generate interface for %s.\n", messageDeclaration.getType());
            e.printStackTrace();
        }
    }

    /**
     * @param outputDirectory
     * @param packages
     * @param packagePath
     */
    public final void generate(
            final File outputDirectory
            , final Collection<String> packages
            , final Collection<File> packagePath) {

        for (final File directory : packagePath) {
            this.topicDefinitionFileProvider.addDirectory(directory);
            this.serviceDefinitionFileProvider.addDirectory(directory);
            this.actionDefinitionFileProvider.addDirectory(directory);
        }
        this.topicDefinitionFileProvider.update();
        this.serviceDefinitionFileProvider.update();
        this.actionDefinitionFileProvider.update();
        try {
            this.writeTopicInterfaces(outputDirectory, packages);
            this.writeServiceInterfaces(outputDirectory, packages);
            this.writeActionInterfaces(outputDirectory, packages);
        } catch (final IOException ioException) {
            throw new RosMessageRuntimeException(ioException);
        }
    }



    public final int getSuccessfulInterfaceGenerations() {
        return this.successfulInterfaceGenerations;
    }

    public final int getFailedInterfaceGenerations() {
        return this.failedInterfaceGenerations;
    }

    public final int getTotalInterfaceGenerations() {
        return this.getSuccessfulInterfaceGenerations() + this.getFailedInterfaceGenerations();
    }
}
