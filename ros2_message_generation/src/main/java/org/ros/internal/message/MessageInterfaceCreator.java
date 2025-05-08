/*
 * Copyright (C) 2025 Spyros Koukas
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.ros.exception.RosMessageRuntimeException;
import org.ros.internal.message.context.MessageContext;
import org.ros.internal.message.context.MessageContextProvider;
import org.ros.internal.message.field.Field;
import org.ros.internal.message.field.FieldType;
import org.ros.internal.message.field.MessageFields;
import org.ros.internal.message.field.PrimitiveFieldType;
import org.ros.message.MessageDeclarationImpl;
import org.ros.message.MessageFactory;
import org.ros2.interfaces.Ros2InterfaceCategory;
import org.ros2.interfaces.Ros2InterfaceDefinitionRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author https://github.com/SpyrosKou Spyros Koukas
 * @author damonkohler@google.com (Damon Kohler)
 */
public final record MessageInterfaceCreator(MessageDeclarationImpl messageDeclaration
        , String packageName
        , String interfaceName
        , Class<?> javaImplementingInterface
        , Class<?> javaDefinitionInterface
        , String javaDefinitionClassName) {


    public final String build(final MessageFactory messageFactory) {
        Preconditions.checkNotNull(messageDeclaration);
        Preconditions.checkNotNull(packageName);
        Preconditions.checkNotNull(interfaceName);
        Preconditions.checkNotNull(javaImplementingInterface);
        Preconditions.checkNotNull(javaDefinitionInterface);
        Preconditions.checkNotNull(javaDefinitionClassName);
        final StringBuilder builder = new StringBuilder();


        final MessageContextProvider messageContextProvider = new MessageContextProvider(messageFactory);
        final MessageContext messageContext = messageContextProvider.get(messageDeclaration);

        final String fieldsDeclarations = this.createRecordConstructorSignature(messageContext);

        if (this.packageName != null) {
            builder.append(String.format("package %s;\n\n", packageName));
        }

//        builder.append("import " + javaImplementingInterface.getCanonicalName() + ";\n");
//        builder.append("import " + javaDefinitionInterface.getCanonicalName() + ";\n");
        builder.append("import " + messageDeclaration.getPackage() + "." + javaDefinitionClassName + ";\n");
        builder.append("import " + JsonIgnore.class.getCanonicalName() + ";\n");
        builder.append("import " + JsonProperty.class.getCanonicalName() + ";\n");
        builder.append("import " + String.class.getCanonicalName() + ";\n");

        builder.append("\n");

        builder.append(String.format(
                "public record %s(\n %s) implements %s {\n", interfaceName, fieldsDeclarations, javaImplementingInterface.getCanonicalName()));

        this.appendConstants(messageContext, builder);

        builder.append(String.format("public static final String INTERFACE_TYPE = \"%s\";\n",
                messageDeclaration.getType()));
//        builder.append(String.format("public static final String INTERFACE_DEFINITION = \"%s\";\n",
//                JavaStringEscaper.escapeJava(messageDeclaration.getDefinition())));

        builder.append(" @JsonIgnore\n @Override\n public final String interfaceType(){ return INTERFACE_TYPE;}\n");

        builder.append(" @JsonIgnore\n public static final String getInterfaceType(){ return INTERFACE_TYPE;}\n");

        this.getJavaTopLevelDefinitionCode(builder);

        builder.append("}\n");
        return builder.toString();
    }

    private final void getJavaTopLevelDefinitionCode(final StringBuilder builder) {
        if (this.javaDefinitionClassName == Ros2InterfaceDefinitionRecord.class.getCanonicalName()) {

            builder.append(String.format(" public final %s TOP_LEVEL_DEFINITION_INSTANCE=$s(%s,$s,$s,$s);\n"
                    , this.javaDefinitionClassName
                    , this.javaDefinitionClassName
                    , Ros2InterfaceCategory.class.getCanonicalName() + ".MESSAGE"
                    , this.messageDeclaration.getPackage()
                    , this.messageDeclaration.getType()
                    , this.messageDeclaration.getDefinition()));
            builder.append(String.format("  public static final %s getTopLevelDefinition(){ return TOP_LEVEL_DEFINITION_INSTANCE;}\n", this.javaDefinitionInterface.getCanonicalName()));
            builder.append(String.format(" @JsonIgnore\n @Override\n public final %s topLevelDefinition(){ return TOP_LEVEL_DEFINITION_INSTANCE;}\n", this.javaDefinitionInterface.getCanonicalName()));
        } else {

            builder.append(String.format(" @JsonIgnore\n @Override\n public final %s topLevelDefinition(){ return %s.get();}\n", this.javaDefinitionInterface.getCanonicalName(), this.javaDefinitionClassName));
        }
    }


    @SuppressWarnings("deprecation")
    private final String getJavaValue(final PrimitiveFieldType primitiveFieldType, final String value) {
        switch (primitiveFieldType) {
            case BOOL:
                return Boolean.valueOf(!value.equals("0") && !value.equals("false")).toString();
            case FLOAT32:
                return value + "f";
            case STRING:
            case WSTRING:
                return "\"" + JavaStringEscaper.escapeJava(value) + "\"";
            case BYTE:
            case CHAR:
            case INT8:
            case UINT8:
            case INT16:
            case UINT16:
            case INT32:
            case UINT32:
            case INT64:
            case UINT64:
            case FLOAT64:
                return value;
            default:
                throw new RosMessageRuntimeException("Unsupported PrimitiveFieldType: " + primitiveFieldType);
        }
    }

    private final void appendConstants(final MessageContext messageContext, final StringBuilder builder) {
        final MessageFields messageFields = new MessageFields(messageContext);
        final boolean constantsExist = messageFields.getFields().stream().anyMatch(Field::isConstant);
        if (constantsExist) {
            builder.append("\n //--------Constants Definitions Start----------\n\n");
        }
        for (final Field field : messageFields.getFields()) {
            if (field.isConstant()) {
                Preconditions.checkState(field.getType() instanceof PrimitiveFieldType);
                // We use FieldType and cast back to PrimitiveFieldType below to avoid a
                // bug in the Sun JDK: http://gs.sun.com/view_bug.do?bug_id=6522780
                final FieldType fieldType = (FieldType) field.getType();
                final String value = getJavaValue((PrimitiveFieldType) fieldType, field.getValue().toString());
                builder.append(String.format("public static final %s %s = %s;\n", fieldType.getJavaTypeName(),
                        field.getName(), value));
            }
        }
        if (constantsExist) {
            builder.append("\n //--------Constants Definitions End----------\n\n");
        }
    }

    private final String createRecordConstructorSignature(MessageContext messageContext) {

        final MessageFields messageFields = new MessageFields(messageContext);
        final List<String> fieldDeclarations = new ArrayList<>(messageFields.getFields().size());
        final Multimap<String, String> fieldNames = HashMultimap.create();
        for (final Field field : messageFields.getFields()) {
            if (field.isConstant()) {
                continue;
            }
            final String type = field.getJavaTypeName();
            final String name = field.getName();
            final String nameToCaps = name.toUpperCase();
            fieldNames.put(nameToCaps, name);
            final String declaration = String.format("@JsonProperty(\"%s\") %s %s\n", name, type, name);
            fieldDeclarations.add(declaration);
        }
        final StringJoiner stringJoiner = new StringJoiner(",");
        fieldDeclarations.forEach(stringJoiner::add);
        for (final String nameCaps : fieldNames.keySet()) {
            final Collection<String> values = fieldNames.get(nameCaps);
            if (values.size() > 1) {
                System.err.println("WARNING: Interface:[" + messageContext.getType() + "] fields:" + values + " have only cap differences with: " + values.stream().findAny().get() + ". This is discouraged in ROS 2.0 ");
            }
        }
        return stringJoiner.toString();
    }

}
