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
 * Models a service or action high-level ROS2 interface.
 * @author Spyros Koukas
 */
public interface Ros2InterfaceDefinition {
   /**
    * The {@link Ros2InterfaceType} of this interface.
    * It should be a  {@link Ros2InterfaceType#MESSAGE} or a {@link Ros2InterfaceType#ACTION} or a {@link Ros2InterfaceType#SERVICE}.
    * @return
    */
   Ros2InterfaceType interfaceType();

    /**
     * The name of this interface e.g. `example_interfaces/srv/AddTwoInts`
    * @return
    */
   String interfaceName();

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
