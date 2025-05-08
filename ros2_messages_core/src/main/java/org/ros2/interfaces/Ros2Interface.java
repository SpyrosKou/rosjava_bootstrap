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
 * @author Spyros Koukas
 */
public interface Ros2Interface{
   /**
    * The {@link Ros2InterfaceType} of this interface
    * @return
    */
   @JsonIgnore
   Ros2InterfaceType interfaceType();

   /**
    * The name of this interface e.g. `example_interfaces/msg/String`
    * @return
    */
   @JsonIgnore
   String interfaceName();

   /**
    * For actions or services, this method returns the complete definition of the service and action respectively.
    * For topics, this method returns the definition of the topic.
    * @return
    */
   @JsonIgnore
   Ros2InterfaceDefinition topLevelDefinition();
}
