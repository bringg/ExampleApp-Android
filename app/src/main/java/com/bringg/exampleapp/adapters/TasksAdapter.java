package com.bringg.exampleapp.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bringg.exampleapp.R;
import com.bringg.exampleapp.utils.Utils;

import java.util.List;

import driver_sdk.models.tasks.Task;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TaskViewHolder> {


    private final List<Task> mItems;
    private final TasksAdapterListener mListener;

    public TasksAdapter(List<Task> items, TasksAdapterListener listener) {
        mItems = items;
        mListener = listener;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        holder.onBind(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTvTitle;
        private final TextView mTvDateStart;

        public TaskViewHolder(View itemView) {
            super(itemView);
            mTvTitle = itemView.findViewById(R.id.tv_task_title);
            mTvDateStart = itemView.findViewById(R.id.tv_task_date_start);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onItemSelected(mItems.get(getAdapterPosition()));
                }
            });

        }

        public void onBind(Task task) {
            mTvTitle.setText(task.getExtendedAddress());
            mTvDateStart.setText(Utils.isoToStringDate(task.getScheduledAt()));
        }
    }

    public interface TasksAdapterListener {
        void onItemSelected(Task task);
    }
}
