/*
 * Copyright (C) 2025 Spyros Koukas
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
import org.ros.message.MessageDeclarationImpl;
import org.ros.message.MessageFactory;
import org.ros2.interfaces.Ros2ActionDefinition;
import org.ros2.interfaces.Ros2ServiceDefinition;

/**
 * Generates the ROS2 Action Definition classes
 * @author https://github.com/SpyrosKou Spyros Koukas
 *
 */
public final record ActionDefinitionCreator(
        MessageDeclarationImpl messageDeclaration
        , String goalClass
        , String feedbackClass
        , String resultClass) {


    public final String build(final MessageFactory messageFactory) {
        Preconditions.checkNotNull(messageDeclaration);
        Preconditions.checkNotNull(goalClass);
        Preconditions.checkNotNull(feedbackClass);
        Preconditions.checkNotNull(resultClass);
        final StringBuilder builder = new StringBuilder();

        if (this.messageDeclaration.getPackage() != null) {
            builder.append(String.format("package %s;\n\n", this.messageDeclaration.getPackage()));
        }

        builder.append("import " + Ros2ActionDefinition.class.getCanonicalName() + ";\n");
        builder.append("import " + this.messageDeclaration.getPackage()+"."+goalClass + ";\n");
        builder.append("import " + this.messageDeclaration.getPackage()+"."+feedbackClass + ";\n");
        builder.append("import " + this.messageDeclaration.getPackage()+"."+resultClass + ";\n");
        builder.append("import java.lang.String;");

        builder.append("\n\n\n");

        builder.append(String.format(
                "public final class %s implements %s {\n\n\n",messageDeclaration.getName(), Ros2ActionDefinition.class.getName()));

        builder.append(" /**\n *\n * Singleton access \n *\n **/\n");
        builder.append(String.format(
                " private %s %s(){};",messageDeclaration.getName(),messageDeclaration.getName()));

        builder.append(" /**\n *\n * A singleton instance \n *\n **/\n");
        builder.append(String.format(
                " public static final %s INSTANCE= new %s();",messageDeclaration.getName(),messageDeclaration.getName()));

        builder.append(" /**\n *\n * Get the singleton instance \n *\n **/\n");
        builder.append(String.format(
                " public static final %s get(){return INSTANCE;}",messageDeclaration.getName()));

        //Constants
        builder.append(" /**\n *\n * The type of the interface e.g. <a href=\"https://docs.ros2.org/foxy/api/example_interfaces/action/Fibonacci.html\">example_interfaces/action/Fibonacci</a>. \n *\n **/\n");
        builder.append(String.format(" public static final String INTERFACE_TYPE = \"%s\";\n\n\n",
                messageDeclaration.getType()));

        builder.append(" /**\n *\n * The name of the interface e.g. `example_interfaces' \n *\n **/\n");
        builder.append(String.format(" public static final String INTERFACE_NAME = \"%s\";\n\n\n",
                messageDeclaration.getName()));

        builder.append(" /**\n *\n * The package name of the interface e.g. `example_interfaces' \n *\n **/\n");
        builder.append(String.format(" public static final String PACKAGE_NAME = \"%s\";\n\n\n",
                messageDeclaration.getPackage()));

        builder.append(" /**\n *\n * The contents of the interface file definition e.g. the contents of <a href=\"https://docs.ros2.org/foxy/api/example_interfaces/action/Fibonacci.html\">example_interfaces/action/Fibonacci</a> \n *\n **/\n");
        builder.append(String.format(" public static final String DEFINITION = \"%s\";\n\n\n",
                JavaStringEscaper.escapeJava(messageDeclaration.getDefinition())));

        builder.append(" /**\n *\n * The class modeling the goal of the action  e.g. `example_interfaces/action/FibonacciGoal' \n *\n **/\n");
        builder.append(String.format(" public static final Class<Ros2ActionGoalInterface> GOAL_CLASS = %s.class;\n\n\n",goalClass));

        builder.append(" /**\n *\n * The class modeling the feedback of the action  e.g. `example_interfaces/action/FibonacciFeedback' \n *\n **/\n");
        builder.append(String.format(" public static final Class<Ros2ActionFeedbackInterface> FEEDBACK_CLASS = %s.class;\n\n\n",feedbackClass));

        builder.append(" /**\n *\n * The class modeling the result of the action  e.g. `example_interfaces/action/FibonacciResult' \n *\n **/\n");
        builder.append(String.format(" public static final Class<Ros2ActionResultInterface> RESULT_CLASS = %s.class;\n\n\n",resultClass));
        //Static methods
        builder.append(" /**\n *\n * The type of the interface e.g. <a href=\"https://docs.ros2.org/foxy/api/example_interfaces/action/Fibonacci.html\">example_interfaces/action/Fibonacci</a>. \n *\n **/\n");
        builder.append(" public static final String getInterfaceType(){ return INTERFACE_TYPE;}\n\n");

        builder.append(" /**\n *\n * The name of the interface e.g. `example_interfaces' \n *\n **/\n");
        builder.append(" public static final String getInterfaceName(){ return INTERFACE_NAME;}\n\n");

        builder.append(" /**\n *\n * The package name of the interface e.g. `example_interfaces' \n *\n **/\n");
        builder.append(" public static final String getPackageName(){ return PACKAGE_NAME;}\n\n");

        builder.append(" /**\n *\n * The contents of the interface file definition e.g. the contents of <a href=\"https://docs.ros2.org/foxy/api/example_interfaces/action/Fibonacci.html\">example_interfaces/action/Fibonacci</a> \n *\n **/\n");
        builder.append(" public static final String getDefinition(){ return DEFINITION;}\n\n");

        builder.append(" /**\n *\n * The class modeling the goal of the action  e.g. `example_interfaces/action/FibonacciGoal' \n *\n **/\n");
        builder.append(" public static final Class<Ros2ActionGoalInterface> getGoalClass(){ return GOAL_CLASS;}\n\n");

        builder.append(" /**\n *\n * The class modeling the feedback of the action  e.g. `example_interfaces/action/FibonacciFeedback' \n *\n **/\n");
        builder.append(" public static final Class<Ros2ActionFeedbackInterface> getFeedbackClass(){ return FEEDBACK_CLASS;}\n\n");

        builder.append(" /**\n *\n * The class modeling the result of the action  e.g. `example_interfaces/action/FibonacciResult' \n *\n **/\n");
        builder.append(" public static final Class<Ros2ActionResultInterface> getResultClass(){ return RESULT_CLASS;}\n\n");
        //Instance methods
        builder.append(" /**\n *\n * The type of the interface e.g. <a href=\"https://docs.ros2.org/foxy/api/example_interfaces/action/Fibonacci.html\">example_interfaces/action/Fibonacci</a>. \n *\n **/\n");
        builder.append(" public final String interfaceType(){ return INTERFACE_TYPE;}\n\n");

        builder.append(" /**\n *\n * The name of the interface e.g. `example_interfaces' \n *\n **/\n");
        builder.append(" public final String interfaceName(){ return INTERFACE_NAME;}\n\n");

        builder.append(" /**\n *\n * The package name of the interface e.g. `example_interfaces' \n *\n **/\n");
        builder.append(" public final String packageName(){ return PACKAGE_NAME;}\n\n");

        builder.append(" /**\n *\n * The contents of the interface file definition e.g. the contents of <a href=\"https://docs.ros2.org/foxy/api/example_interfaces/action/Fibonacci.html\">example_interfaces/action/Fibonacci</a> \n *\n **/\n");
        builder.append(" public final String definition(){ return DEFINITION;}\n\n");

        builder.append(" /**\n *\n * The class modeling the goal of the action  e.g. `example_interfaces/action/FibonacciGoal' \n *\n **/\n");
        builder.append(" public final Class<Ros2ActionGoalInterface> goalClass(){ return GOAL_CLASS;}\n\n");

        builder.append(" /**\n *\n * The class modeling the feedback of the action  e.g. `example_interfaces/action/FibonacciFeedback' \n *\n **/\n");
        builder.append(" public static final Class<Ros2ActionFeedbackInterface> feedbackClass(){ return FEEDBACK_CLASS;}\n\n");

        builder.append(" /**\n *\n * The class modeling the result of the action  e.g. `example_interfaces/action/FibonacciResult' \n *\n **/\n");
        builder.append(" public final Class<Ros2ActionResultInterface> resultClass(){ return RESULT_CLASS;}\n\n");

        //close class
        builder.append("\n}\n");
        return builder.toString();
    }

}
