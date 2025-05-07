package org.ros.message;

public interface MessageIdentifier {
    String getType();

    String getPackage();

    String getName();
}
