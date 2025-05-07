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
package ros2.interfaces.action;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Modelled after https://docs.ros2.org/foxy/api/action_msgs/msg/GoalStatus.html
 *
 * @author Spyros Koukas
 */
public enum Ros2ActionGoalStatus {
    STATUS_UNKNOWN((byte) 0),
    STATUS_ACCEPTED((byte) 1),
    STATUS_EXECUTING((byte) 2),
    STATUS_CANCELING((byte) 3),
    STATUS_SUCCEEDED((byte) 4),
    STATUS_CANCELED((byte) 5),
    STATUS_ABORTED((byte) 6);

    private final byte status;
    private static final ConcurrentHashMap<Byte, Ros2ActionGoalStatus> STATUS_MAP = Ros2ActionGoalStatus.createMap();

    private Ros2ActionGoalStatus(final byte status) {
        this.status = status;
    }

    private static final ConcurrentHashMap<Byte, Ros2ActionGoalStatus> createMap() {
        final ConcurrentHashMap<Byte, Ros2ActionGoalStatus> map = new ConcurrentHashMap<>(Ros2ActionGoalStatus.values().length);
        for (final Ros2ActionGoalStatus actionGoalStatus : Ros2ActionGoalStatus.values()) {
            map.put(Byte.valueOf(actionGoalStatus.getStatus()), actionGoalStatus);
        }
        return map;
    }

    public final byte getStatus() {
        return status;
    }

    public static final Ros2ActionGoalStatus fromStatus(final byte status) {
        final var result = Ros2ActionGoalStatus.STATUS_MAP.get(status);
        if (result == null) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        return result;
    }
}
