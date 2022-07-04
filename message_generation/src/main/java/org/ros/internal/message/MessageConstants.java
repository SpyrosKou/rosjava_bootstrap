package org.ros.internal.message;

/**
 * Constants related to ROS messages
 * Created at 2022-06-18 on 13:46
 *
 * @author Spyros Koukas
 */
public final class MessageConstants {
    public static final String REQUEST = "Request";
    public static final String SRV = "srv";
    public static final String RESPONSE = "Response";
    public static final String MSG = "msg";
    public static final String ACTION = "action";
    public static final String JAVA = ".java";

    private MessageConstants() {
        throw new UnsupportedOperationException();
    }


}
