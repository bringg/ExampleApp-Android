package com.bringg.exampleapp;

import android.support.annotation.NonNull;

public interface WaypointPresenter {

    void showDialogNotInShift();

    void updateViews();

    void handleTaskDone(long taskId);

    void handleNextWaypointStarted(long nextWayPointId);

    void handleWaypointDone(long wayPointId);

    void onError(int errorCode, @NonNull String message);
}
