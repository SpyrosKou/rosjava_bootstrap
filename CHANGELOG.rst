Changelog
=========

0.1 ROS2 (2025-05-07)
------------------
* Initial fork from 0.3.8 noetic
* Start as a 'new' ROS2 oriented version, targeting ROS2 Jazzy
* Gradle upgrade to version 8.14
* Delete gradle_plugins, and experiment folders
* Added ros2_messages_core
* Renamed message_generation -> ros2_message_generation
* Renamed packages_parser -> ros2_packages_parser
* Completely new representation in records, annotated with @JacksonProperty
* Includes metadata about each interface
* Tests using example_interfaces(https://github.com/ros2/example_interfaces) , common_interfaces(https://github.com/ros2/common_interfaces/ )  and rcl_interfaces (https://github.com/ros2/rcl_interfaces)
* Fields whose name differs only in the caps are allowed but warned. e.g. Foo and foo. Instead of keeping only the first one which was the previous behaviour.interface
* Serialization and MD5 are out of scope
