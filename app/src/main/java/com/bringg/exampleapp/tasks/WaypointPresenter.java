package com.bringg.exampleapp.tasks;

import android.support.annotation.NonNull;

interface WaypointPresenter {
    void showDialogNotInShift();

    void updateViews();

    void showError(@NonNull String message);

    void handleTaskDone(long taskId);

    void handleNextWaypointStarted(long nextWayPointId);

    void handleWaypointDone(long wayPointId);
}
