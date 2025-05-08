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
package org.ros2.interfaces.action;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.ros2.interfaces.Ros2Interface;
import org.ros2.interfaces.Ros2InterfaceDefinition;
import org.ros2.interfaces.Ros2InterfaceCategory;
//TODO replace goal_id type with UUID

/**
 * @author Spyros Koukas
 */
public record FeedbackMessage<T extends Ros2Interface>(@JsonProperty("goal_id") String goal_id,
                                                       @JsonProperty("feedback") T feedback) implements Ros2Interface {
    private static final String FEEDBACK_MESSAGE_POSTFIX = "_FeedbackMessage";
    private static final String TYPE_DEFINITION = "unique_identifier_msgs/UUID goal_id\n";

    @Override
    @JsonIgnore
    public final Ros2InterfaceCategory interfaceCategory() {
        return Ros2InterfaceCategory.ACTION_FEEDBACK_WRAPPER;
    }

    /**
     * Returns a generated {@link Ros2Interface#interfaceType()}
     *
     * @return
     */
    @Override
    @JsonIgnore
    public final String interfaceType() {
        return this.feedback().interfaceType() + FEEDBACK_MESSAGE_POSTFIX;
    }


    @Override
    @JsonIgnore
    public final Ros2InterfaceDefinition topLevelDefinition() {
        return this.feedback().topLevelDefinition();
    }
}
