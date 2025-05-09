package org.ros2.message;

public interface MessageIdentifier {
    String getType();

    String getPackage();

    String getName();
}
