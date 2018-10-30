package com.bringg.exampleapp.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bringg.exampleapp.R;
import com.bringg.exampleapp.utils.Utils;

import java.util.List;

import driver_sdk.models.Task;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TaskViewHolder> {

    private final List<Task> mItems;
    private final TasksAdapterListener mListener;

    public TasksAdapter(List<Task> items, TasksAdapterListener listener) {
        mItems = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.onBind(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public interface TasksAdapterListener {
        void onItemSelected(long taskId);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTvAddress;
        private final TextView mTvDateStart;
        private final TextView mTvTitle;
        private final View mTvNotAccepted;

        TaskViewHolder(View itemView) {
            super(itemView);
            mTvAddress = itemView.findViewById(R.id.tv_task_address);
            mTvTitle = itemView.findViewById(R.id.tv_task_title);
            mTvNotAccepted = itemView.findViewById(R.id.tv_task_accepted);

            mTvDateStart = itemView.findViewById(R.id.tv_task_date_start);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onItemSelected(mItems.get(getAdapterPosition()).getId());
                }
            });

        }

        void onBind(Task task) {
            driver_sdk.models.tasks.Task taskModel = (driver_sdk.models.tasks.Task) task;
            mTvAddress.setText(taskModel.getExtendedAddress());
            mTvDateStart.setText(Utils.isoToStringDate(taskModel.getScheduledAt()));
            mTvTitle.setText(task.getTitle());
            if (task.isAccepted())
                mTvNotAccepted.setVisibility(View.GONE);
            else
                mTvNotAccepted.setVisibility(View.VISIBLE);
        }
    }
}
