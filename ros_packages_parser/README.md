# ROS Packages Parser
* Generates ROS Java message definitions from all the catkin packages found in the folders `ROS_PACKAGE_PATH`.
* In ROS Noetic these are by default:
  - In Windows `C:\opt\ros\noetic\x64\share`
  - In Linux  `/opt/ros/noetic/share`
* The environment variable `ROS_PACKAGE_PATH` should be pointing to the correct folder to use this script