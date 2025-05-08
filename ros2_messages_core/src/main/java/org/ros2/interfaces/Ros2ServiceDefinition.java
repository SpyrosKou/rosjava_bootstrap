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
 * Models a service ROS2 interface.
 * @author Spyros Koukas
 */
public interface Ros2ServiceDefinition extends Ros2InterfaceDefinition {

   @Override
   public default Ros2InterfaceCategory interfaceCategory(){
      return Ros2InterfaceCategory.SERVICE;
   }

   /**
    * The name of the request interface type e.g. `example_interfaces/srv/AddTwoIntsRequest`
    * @return
    */
   Class<Ros2ServiceRequestInterface> requestClass();

   /**
    * The name of the request interface type e.g. `example_interfaces/srv/AddTwoIntsResponse`
    * @return
    */
   Class<Ros2ServiceResponseInterface> responseClass();

}
