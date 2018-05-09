package com.bringg.exampleapp.tasks;

import driver_sdk.models.tasks.Task;
import driver_sdk.models.tasks.Waypoint;

interface TaskListener {

    void actionWayPoint(long wayPointId);

    TaskHelper.TaskState getTaskState();

    TaskHelper.WayPointState getWayPointState(long wayPointId);

    Waypoint getWayPointById(long wayPointId);
}
