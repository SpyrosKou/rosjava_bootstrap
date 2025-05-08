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
package org.ros2.interfaces;


/**
 * The type of interface modelled
 * @author Spyros Koukas
 */
public enum Ros2InterfaceCategory {
    /**
     * The type of interface modeled is a ros2 msg. Msgs can be reused in services and actions definitions or used to declare topics e.g.<a href="https://docs.ros2.org/foxy/api/example_interfaces/msg/String.html">example_interfaces/msg/String</a>.
     */
    MESSAGE,
    /**
     * The high-level service interface model e.g. the <a href="https://docs.ros2.org/foxy/api/example_interfaces/srv/AddTwoInts.html">example_interfaces/srv/AddTwoInts</a>.
     */
    SERVICE,
    /**
     * The type of interface modeled is a service Request. e.g. the Request part of <a href="https://docs.ros2.org/foxy/api/example_interfaces/srv/AddTwoInts.html">example_interfaces/srv/AddTwoInts</a>.
     */
    SERVICE_REQUEST,

    /**
     * The type of interface modeled is a service Response. e.g. the Response part of <a href="https://docs.ros2.org/foxy/api/example_interfaces/srv/AddTwoInts.html">example_interfaces/srv/AddTwoInts</a>.
     */
    SERVICE_RESPONSE,
    /**
     * The high-level action interface model e.g. the <a href="https://docs.ros2.org/foxy/api/example_interfaces/action/Fibonacci.html">example_interfaces/action/Fibonacci</a>.
     */
    ACTION,
    /**
     * The type of interface modeled is an action Goal. e.g. the Goal part of <a href="https://docs.ros2.org/foxy/api/example_interfaces/action/Fibonacci.html">example_interfaces/action/Fibonacci</a>.
     */
    ACTION_GOAL,

    /**
     * The type of interface modeled is an action Feedback. e.g. the Feedback part of <a href="https://docs.ros2.org/foxy/api/example_interfaces/action/Fibonacci.html">example_interfaces/action/Fibonacci</a>.
     */
    ACTION_FEEDBACK,

    /**
     * The ACTION_FEEDBACK as published from the action server, including its goal_id
     */
    ACTION_FEEDBACK_WRAPPER,

    /**
     * The type of interface modeled is an action Result. e.g. the Result part of <a href="https://docs.ros2.org/foxy/api/example_interfaces/action/Fibonacci.html">example_interfaces/action/Fibonacci</a>.
     */
    ACTION_RESULT,

    /**
     * The ACTION_RESULT as returned from the action server, including its appended status.
     */
    ACTION_RESULT_WRAPPER
}
