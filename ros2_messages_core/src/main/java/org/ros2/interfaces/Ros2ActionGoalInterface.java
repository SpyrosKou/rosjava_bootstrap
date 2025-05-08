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
public interface Ros2ActionGoalInterface extends Ros2Interface {
   /**
    * The {@link Ros2InterfaceType} of this interface
    * @return
    */
   @JsonIgnore
   @Override
   public default Ros2InterfaceType interfaceType(){
      return Ros2InterfaceType.ACTION_GOAL;
   }


   @JsonIgnore
   @Override
   Ros2ActionDefinition topLevelDefinition();
}
