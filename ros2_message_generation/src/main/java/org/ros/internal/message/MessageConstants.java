/*
 * Copyright (C) Spyros Koukas
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
