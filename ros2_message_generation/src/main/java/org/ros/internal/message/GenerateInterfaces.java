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

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.ros.exception.RosMessageRuntimeException;
import org.ros.internal.message.action.ActionDefinitionFileProvider;
import org.ros.internal.message.action.ActionGenerationTemplateFeedback;
import org.ros.internal.message.action.ActionGenerationTemplateGoal;
import org.ros.internal.message.action.ActionGenerationTemplateResult;
import org.ros.internal.message.definition.MessageDefinitionProviderChain;
import org.ros.internal.message.definition.MessageDefinitionTupleParser;
import org.ros.internal.message.service.ServiceDefinitionFileProvider;
import org.ros.internal.message.topic.TopicDefinitionFileProvider;
import org.ros.message.MessageDeclarationImpl;
import org.ros.message.MessageFactory;
import org.ros.message.MessageIdentifier;
import org.ros2.interfaces.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

//    private final MessageGenerationTemplate actionGenerationTemplateActionGoal = new ActionGenerationTemplateActionGoal();
//    private final MessageGenerationTemplate actionGenerationTemplateActionResult = new ActionGenerationTemplateActionResult();
//    private final MessageGenerationTemplate actionGenerationTemplateActionFeedback = new ActionGenerationTemplateActionFeedback();


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
            this.writeInterface(messageDeclaration, outputDirectory, Ros2MessageInterface.class, Ros2InterfaceDefinition.class, Ros2InterfaceDefinitionRecord.class.getName());
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
        for (final String pkg : packages) {
            final Set<MessageIdentifier> messageIdentifiers =
                    serviceDefinitionFileProvider.getMessageIdentifiersByPackage(pkg);
            if (messageIdentifiers != null) {
                serviceTypes.addAll(messageIdentifiers);
            }
        }
        for (final MessageIdentifier serviceType : serviceTypes) {
            final String definition = messageDefinitionProviderChain.get(serviceType.getType());
            MessageDeclarationImpl serviceDeclaration =
                    MessageDeclarationImpl.of(serviceType.getType(), definition);
            final String requestPlainType = serviceType.getName() + MessageConstants.REQUEST_POSTFIX;
            final String responsePlainType = serviceType.getName() + MessageConstants.RESPONSE_POSTFIX;
            this.writeServiceInterfaceDefinition(serviceDeclaration, outputDirectory, requestPlainType, responsePlainType);
            List<String> requestAndResponse = MessageDefinitionTupleParser.parse(definition, 2);


            final String requestType = serviceType.getType() + MessageConstants.REQUEST_POSTFIX;
            final String responseType = serviceType.getType() + MessageConstants.RESPONSE_POSTFIX;
            MessageDeclarationImpl requestDeclaration =
                    MessageDeclarationImpl.of(requestType, requestAndResponse.get(0));
            MessageDeclarationImpl responseDeclaration =
                    MessageDeclarationImpl.of(responseType, requestAndResponse.get(1));
            this.writeInterface(requestDeclaration, outputDirectory, Ros2ServiceRequestInterface.class, Ros2ServiceDefinition.class, serviceDeclaration.getName());
            this.writeInterface(responseDeclaration, outputDirectory, Ros2ServiceResponseInterface.class, Ros2ServiceDefinition.class, serviceDeclaration.getName());
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
            final String goalPlainType = actionType.getName() + MessageConstants.GOAL_POSTFIX;
            final String feedbackPlainType = actionType.getName() + MessageConstants.FEEDBACK_POSTFIX;
            final String resultPlainType = actionType.getName() + MessageConstants.RESULT_POSTFIX;
            this.writeActionInterfaceDefinition(actionDeclaration, outputDirectory, goalPlainType, feedbackPlainType, resultPlainType);
            final List<String> goalResultAndFeedback = MessageDefinitionTupleParser.parse(definition, 3);
            {
                final String goalType = actionType.getType() + MessageConstants.GOAL_POSTFIX;
                final String feedbackType = actionType.getType() + MessageConstants.FEEDBACK_POSTFIX;
                final String resultType = actionType.getType() + MessageConstants.RESULT_POSTFIX;
                final MessageDeclarationImpl goalDeclaration = MessageDeclarationImpl.of(
                        goalType,
                        actionGenerationTemplateGoal.applyTemplate(goalResultAndFeedback.get(0))
                );
                final MessageDeclarationImpl resultDeclaration = MessageDeclarationImpl.of(
                        resultType,
                        actionGenerationTemplateResult.applyTemplate(goalResultAndFeedback.get(1))
                );
                final MessageDeclarationImpl feedbackDeclaration = MessageDeclarationImpl.of(
                        feedbackType,
                        actionGenerationTemplateFeedback.applyTemplate(goalResultAndFeedback.get(2))
                );

//            final MessageDeclarationImpl actionGoalDeclaration = MessageDeclarationImpl.of(
//                    actionType.getType() + "ActionGoal",
//                    actionGenerationTemplateActionGoal.applyTemplate(actionType.getType())
//            );
//            final MessageDeclarationImpl actionResultDeclaration = MessageDeclarationImpl.of(
//                    actionType.getType() + "ActionResult",
//                    actionGenerationTemplateActionResult.applyTemplate(actionType.getType())
//            );
//            final MessageDeclarationImpl actionFeedbackDeclaration = MessageDeclarationImpl.of(
//                    actionType.getType() + "ActionFeedback",
//                    actionGenerationTemplateActionFeedback.applyTemplate(actionType.getType())
//            );


                this.writeInterface(goalDeclaration, outputDirectory, Ros2ActionGoalInterface.class, Ros2ActionDefinition.class, actionDeclaration.getName());
                this.writeInterface(resultDeclaration, outputDirectory, Ros2ActionResultInterface.class, Ros2ActionDefinition.class, actionDeclaration.getName());
                this.writeInterface(feedbackDeclaration, outputDirectory, Ros2ActionFeedbackInterface.class, Ros2ActionDefinition.class, actionDeclaration.getName());
//            this.writeInterface(actionGoalDeclaration, outputDirectory, true);
//            this.writeInterface(actionResultDeclaration, outputDirectory, true);
//            this.writeInterface(actionFeedbackDeclaration, outputDirectory, true);
            }
        }
    }


    private final void writeInterface(
            final MessageDeclarationImpl messageDeclaration
            , final File outputDirectory
            , final Class<?> javaImplementingInterface
            , final Class<?> javaDefinitionInterface
            , final String javaDefinitionClassName) {
        Preconditions.checkNotNull(messageDeclaration);
        Preconditions.checkNotNull(outputDirectory);
        Preconditions.checkNotNull(javaImplementingInterface);
        Preconditions.checkNotNull(javaDefinitionInterface);
        Preconditions.checkNotNull(javaDefinitionClassName);

        final MessageInterfaceCreator builder = new MessageInterfaceCreator(
                messageDeclaration
                , messageDeclaration.getPackage()
                , messageDeclaration.getName()
                , javaImplementingInterface
                , javaDefinitionInterface
                , javaDefinitionClassName);

        try {
            final String content = builder.build(this.messageFactory);
            final File file = new File(outputDirectory, messageDeclaration.getType() + MessageConstants.JAVA);
            FileUtils.writeStringToFile(file, content, Charset.defaultCharset());
            this.successfulInterfaceGenerations++;
        } catch (Exception e) {
            this.failedInterfaceGenerations++;
            System.out.printf("Failed to generate interface for %s.\n", messageDeclaration.getType());
            e.printStackTrace();
        }
    }

    /**
     * Generates and writes the Java service interface definition for a service based
     * on the provided message declaration, request type, and response type. This method
     * creates the service interface content and saves it to a file within the specified
     * output directory, using a name derived from the message type.
     *
     * @param messageDeclaration the message declaration containing the details necessary
     *                           to generate the service interface
     * @param outputDirectory    the directory where the generated service interface file
     *                           will be stored
     * @param requestType        the message type for the request part of the service
     * @param responseType       the message type for the response part of the service
     */
    private final void writeServiceInterfaceDefinition(final MessageDeclarationImpl messageDeclaration, final File outputDirectory, final String requestType, final String responseType) {
        final ServiceDefinitionCreator contentCreator = new ServiceDefinitionCreator(
                messageDeclaration
                , requestType
                , responseType
        );

        try {
            final String content = contentCreator.build(this.messageFactory);
            final File file = new File(outputDirectory, messageDeclaration.getType() + MessageConstants.JAVA);
            FileUtils.writeStringToFile(file, content, Charset.defaultCharset());
            this.successfulInterfaceGenerations++;
        } catch (Exception e) {
            this.failedInterfaceGenerations++;
            System.err.printf("Failed to generate interface for %s.\n", messageDeclaration.getType());
            e.printStackTrace();
        }
    }

    /**
     * Writes the Java interface definition for an action based on the provided message declaration,
     * to the specified output directory. This method generates the interface content and writes
     * it into a file with a name derived from the message type.
     *
     * @param messageDeclaration the message declaration containing details used to generate the interface
     * @param outputDirectory    the directory where the generated interface file will be written
     * @param goalType           the message type for the goal part of the action
     * @param feedbackType       the message type for the feedback part of the action
     * @param resultType         the message type for the result part of the action
     */
    private final void writeActionInterfaceDefinition(final MessageDeclarationImpl messageDeclaration, final File outputDirectory, final String goalType, final String feedbackType, final String resultType) {
        final ActionDefinitionCreator contentCreator = new ActionDefinitionCreator(
                messageDeclaration
                , goalType
                , feedbackType
                , resultType
        );

        try {
            final String content = contentCreator.build(this.messageFactory);
            final File file = new File(outputDirectory, messageDeclaration.getType() + MessageConstants.JAVA);
            FileUtils.writeStringToFile(file, content, Charset.defaultCharset());
            this.successfulInterfaceGenerations++;
        } catch (Exception e) {
            this.failedInterfaceGenerations++;
            System.err.printf("Failed to generate interface for %s.\n", messageDeclaration.getType());
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
