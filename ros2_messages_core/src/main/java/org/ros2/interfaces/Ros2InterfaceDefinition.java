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
 * Models a service or action high-level ROS2 interface.
 * @author Spyros Koukas
 */
public interface Ros2InterfaceDefinition {
   /**
    * The {@link Ros2InterfaceCategory} of this interface.
    * It should be a  {@link Ros2InterfaceCategory#MESSAGE} or a {@link Ros2InterfaceCategory#ACTION} or a {@link Ros2InterfaceCategory#SERVICE}.
    * @return
    */
   @JsonIgnore
   Ros2InterfaceCategory interfaceCategory();

    /**
     * The type of this interface e.g. `example_interfaces/srv/AddTwoInts`
    * @return
    */
   String interfaceType();

   /**
    * The package name of this interface e.g. `example_interfaces`
    * @return
    */
   String packageName();

   /**
    * This method returns the interface definition of the message, service or action.
    * The definition is the content of the *.msg, *.srv or *.action file that defined the interface.
    * @return
    */
   String definition();
}
