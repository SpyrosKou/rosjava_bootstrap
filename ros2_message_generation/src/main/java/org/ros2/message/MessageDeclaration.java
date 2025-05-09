package org.ros2.message;

public interface MessageDeclaration {
    MessageIdentifier getMessageIdentifier();

    String getType();

    String getPackage();

    String getName();

    String getDefinition();
}
