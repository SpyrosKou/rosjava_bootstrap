package org.ros.internal.message.service;

import org.ros.message.MessageDeclaration;

public interface ServiceDescription extends MessageDeclaration {
    String getMd5Checksum();

    String getRequestType();

    String getRequestDefinition();

    String getResponseType();

    String getResponseDefinition();
}
