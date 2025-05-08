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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Models an action ROS2 interface.
 *
 * @author Spyros Koukas
 */
public interface Ros2ActionDefinition extends Ros2InterfaceDefinition {

    @Override
    @JsonIgnore
    public default Ros2InterfaceCategory interfaceCategory() {
        return Ros2InterfaceCategory.ACTION;
    }

    /**
     * The class that models the Goal of the action
     *
     * @return
     */
    @JsonIgnore
    <GOAL extends Ros2ActionGoalInterface> Class<GOAL> goalClass();


    /**
     * The class that models the Feedback of the action
     *
     * @return
     */
    @JsonIgnore
    <FEEDBACK extends Ros2ActionFeedbackInterface> Class<FEEDBACK> feedbackClass();

    /**
     * The class that models the Result of the action
     *
     * @return
     */
    @JsonIgnore
    <RESULT extends Ros2ActionResultInterface> Class<RESULT> resultClass();

}
