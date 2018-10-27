package com.bringg.exampleapp.tasks;

import driver_sdk.models.Task;
import driver_sdk.models.Waypoint;

interface TaskListener {

    void actionWayPoint(long wayPointId);

    TaskHelper.TaskState getTaskState();

    TaskHelper.WayPointState getWayPointState(long wayPointId);

    Waypoint getWayPointById(long wayPointId);
}
