package org.ros.internal.message;

/**
 * Constants related to ROS messages
 * Created at 2022-06-18 on 13:46
 *
 * @author Spyros Koukas
 */
public final class MessageConstants {
    static final String REQUEST = "Request";
    public static final String SRV = "srv";
    static final String RESPONSE = "Response";
    public static final String MSG = "msg";
    public static final String ACTION = "action";
    static final String JAVA = ".java";
    static final String HEADER_MESSAGE_TYPE = "std_msgs/Header";
    static final String SEQUENCE_FIELD_NAME = "seq";

    private MessageConstants() {
        throw new UnsupportedOperationException();
    }


}
