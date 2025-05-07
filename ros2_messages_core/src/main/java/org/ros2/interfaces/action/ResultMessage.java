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
import org.ros2.interfaces.Ros2InterfaceType;

/**
 * @author Spyros Koukas
 */
public record ResultMessage<T extends Ros2Interface>(@JsonProperty("status") byte status,
                                                     @JsonProperty("result") T result) implements Ros2Interface {
    private static final String TYPE_POSTFIX = "_GetResult_Response";
    private static final String TYPE_DEFINITION = "byte status\n";


    @Override
    @JsonIgnore
    public final Ros2InterfaceType interfaceType() {
        return Ros2InterfaceType.ACTION_FEEDBACK_WRAPPER;
    }

    /**
     * Returns a generated {@link Ros2Interface#interfaceName()}
     *
     * @return
     */
    @Override
    @JsonIgnore
    public final String interfaceName() {
        return this.result().interfaceName() + TYPE_POSTFIX;
    }

    /**
     * Returns a generated {@link Ros2Interface#definition()}
     *
     * @return
     */
    @Override
    @JsonIgnore
    public final String definition() {
        return TYPE_DEFINITION + this.result().definition() + "\n";
    }

    @Override
    @JsonIgnore
    public final String completeDefinition() {
        return this.result().completeDefinition();
    }
}
