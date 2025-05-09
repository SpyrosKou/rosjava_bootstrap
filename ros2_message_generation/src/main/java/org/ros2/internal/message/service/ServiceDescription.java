package org.ros2.internal.message.service;

import org.ros2.message.MessageDeclaration;

public interface ServiceDescription extends MessageDeclaration {
    String getMd5Checksum();

    String getRequestType();

    String getRequestDefinition();

    String getResponseType();

    String getResponseDefinition();
}
