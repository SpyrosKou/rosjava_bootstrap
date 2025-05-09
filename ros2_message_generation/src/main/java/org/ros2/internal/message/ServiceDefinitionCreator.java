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

package org.ros2.internal.message;

import com.google.common.base.Preconditions;
import org.ros2.message.MessageDeclarationImpl;
import org.ros2.message.MessageFactory;
import org.ros2.interfaces.Ros2ServiceDefinition;

/**
 * Generates ROS2 Service Definition classes
 * @author https://github.com/SpyrosKou Spyros Koukas
 *
 */
public final record ServiceDefinitionCreator(
        MessageDeclarationImpl messageDeclaration
        , String requestClass
        , String responseClass) {


    public final String build(final MessageFactory messageFactory) {
        Preconditions.checkNotNull(messageDeclaration);
        Preconditions.checkNotNull(requestClass);
        Preconditions.checkNotNull(responseClass);
        final StringBuilder builder = new StringBuilder();

        if (this.messageDeclaration.getPackage() != null) {
            builder.append(String.format("package %s;\n\n", this.messageDeclaration.getPackage()));
        }

        builder.append("import " + Ros2ServiceDefinition.class.getCanonicalName() + ";\n");
        builder.append("import " + this.messageDeclaration.getPackage()+"."+requestClass + ";\n");
        builder.append("import " + this.messageDeclaration.getPackage()+"."+responseClass + ";\n");
        builder.append("import java.lang.String;");

        builder.append("\n\n\n");

        builder.append(String.format(
                "public final class %s implements %s {\n\n\n",messageDeclaration.getName(), Ros2ServiceDefinition.class.getSimpleName()));

        builder.append(" /**\n *\n * Singleton access \n *\n **/\n");
        builder.append(String.format(
                " private %s(){};",messageDeclaration.getName(),messageDeclaration.getName()));

        builder.append(" /**\n *\n * A singleton instance \n *\n **/\n");
        builder.append(String.format(
                " public static final %s INSTANCE= new %s();",messageDeclaration.getName(),messageDeclaration.getName()));

        builder.append(" /**\n *\n * Get the singleton instance \n *\n **/\n");
        builder.append(String.format(
                " public static final %s get(){return INSTANCE;}",messageDeclaration.getName()));

        //Constants
        builder.append(" /**\n *\n * The type of the interface e.g. `example_interfaces/srv/AddTwoInts' \n *\n **/\n");
        builder.append(String.format(" public static final String INTERFACE_TYPE = \"%s\";\n\n\n",
                messageDeclaration.getType()));

        builder.append(" /**\n *\n * The package name of the interface e.g. `AddTwoInts' \n *\n **/\n");
        builder.append(String.format(" public static final String INTERFACE_NAME = \"%s\";\n\n\n",
                messageDeclaration.getName()));

        builder.append(" /**\n *\n * The package name of the interface e.g. `example_interfaces' \n *\n **/\n");
        builder.append(String.format(" public static final String PACKAGE_NAME = \"%s\";\n\n\n",
                messageDeclaration.getPackage()));

        builder.append(" /**\n *\n * The contents of the interface file definition e.g. the contents of `example_interfaces/srv/AddTwoInts.srv' \n *\n **/\n");
        builder.append(String.format(" public static final String DEFINITION = \"%s\";\n\n\n",
                JavaStringEscaper.escapeJava(messageDeclaration.getDefinition())));

        builder.append(" /**\n *\n * The class modeling the request of the service  e.g. the contents of `example_interfaces/srv/AddTwoIntsRequest' \n *\n **/\n");
        builder.append(String.format(" public static final Class<%s> REQUEST_CLASS = %s.class;\n\n\n",requestClass,requestClass));

        builder.append(" /**\n *\n * The class modeling the response of the service  e.g. the contents of `example_interfaces/srv/AddTwoIntsResponse' \n *\n **/\n");
        builder.append(String.format(" public static final Class<%s> RESPONSE_CLASS = %s.class;\n\n\n",responseClass,responseClass));



        //Static methods ***********************************************************************************************
        builder.append(" /**\n *\n * The type of the interface e.g. `example_interfaces/srv/AddTwoInts' \n *\n **/\n");
        builder.append(" public static final String getInterfaceType(){ return INTERFACE_TYPE;}\n\n");

        builder.append(" /**\n *\n * The package name of the interface e.g. `AddTwoInts' \n *\n **/\n");
        builder.append(" public static final String getInterfaceName(){ return INTERFACE_NAME;}\n\n");

        builder.append(" /**\n *\n * The package name of the interface e.g. `example_interfaces' \n *\n **/\n");
        builder.append(" public static final String getPackageName(){ return PACKAGE_NAME;}\n\n");

        builder.append(" /**\n *\n * The contents of the interface file definition e.g. the contents of `example_interfaces/srv/AddTwoInts.srv' \n *\n **/\n");
        builder.append(" public static final String getDefinition(){ return DEFINITION;}\n\n");

        builder.append(" /**\n *\n * The class modeling the request of the service  e.g. the contents of `example_interfaces/srv/AddTwoIntsRequest' \n *\n **/\n");
        builder.append(String.format(" public static final Class<%s> getRequestClass(){ return REQUEST_CLASS;}\n\n",requestClass));

        builder.append(" /**\n *\n * The class modeling the response of the service  e.g. the contents of `example_interfaces/srv/AddTwoIntsResponse' \n *\n **/\n");
        builder.append(String.format(" public static final Class<%s> getResponseClass(){ return RESPONSE_CLASS;}\n\n",responseClass));


        //Instance methods *********************************************************************************************
        builder.append(" /**\n *\n * The type of the interface e.g. `example_interfaces/srv/AddTwoInts' \n *\n **/\n");
        builder.append(" public final String interfaceType(){ return INTERFACE_TYPE;}\n\n");

        builder.append(" /**\n *\n * The package name of the interface e.g. `AddTwoInts' \n *\n **/\n");
        builder.append(" public final String interfaceName(){ return INTERFACE_NAME;}\n\n");

        builder.append(" /**\n *\n * The package name of the interface e.g. `example_interfaces' \n *\n **/\n");
        builder.append(" public final String packageName(){ return PACKAGE_NAME;}\n\n");

        builder.append(" /**\n *\n * The contents of the interface file definition e.g. the contents of `example_interfaces/srv/AddTwoInts.srv' \n *\n **/\n");
        builder.append(" public final String definition(){ return DEFINITION;}\n\n");

        builder.append(" /**\n *\n * The class modeling the request of the service  e.g. the contents of `example_interfaces/srv/AddTwoIntsRequest' \n *\n **/\n");
        builder.append(String.format(" public final Class<%s> requestClass(){ return REQUEST_CLASS;}\n\n",requestClass));

        builder.append(" /**\n *\n * The class modeling the response of the service  e.g. the contents of `example_interfaces/srv/AddTwoIntsResponse' \n *\n **/\n");
        builder.append(String.format(" public final Class<%s> responseClass(){ return RESPONSE_CLASS;}\n\n",responseClass));

        //close class
        builder.append("\n}\n");
        return builder.toString();
    }

}
