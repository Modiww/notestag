package com.example.notestag;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> tasks;
    private final Context context;
    private final TaskActionListener actionListener;

    public TaskAdapter(Context context, List<Task> tasks, TaskActionListener actionListener) {
        this.context = context;
        this.tasks = tasks;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.textTitle.setText(task.getTitle());
        holder.textStatus.setText(getStatusText(task.getStatus()));
        // сбрасываем иконку галочки в стандартное состояние
        holder.imageDone.setColorFilter(null);
        holder.imageDone.setImageResource(android.R.drawable.checkbox_off_background);

        holder.chipGroupTags.removeAllViews();
        for (String tag : task.getTags()) {
            Chip chip = new Chip(context);
            chip.setText(tag);
            chip.setCheckable(false);
            holder.chipGroupTags.addView(chip);
        }

        // Переключаем статус по нажатию на всю карточку
        holder.itemView.setOnClickListener(v -> {
            int current = task.getStatus();
            int next;
            if (current == Task.STATUS_NOT_STARTED) {
                next = Task.STATUS_IN_PROGRESS;
            } else if (current == Task.STATUS_IN_PROGRESS) {
                next = Task.STATUS_DONE;
            } else {
                next = Task.STATUS_NOT_STARTED;
            }
            task.setStatus(next);
            notifyItemChanged(holder.getAdapterPosition());
        });

        // Галочка выполнено: зелёная и сообщает наружу, что задачу нужно удалить
        holder.imageDone.setOnClickListener(v -> {
            // Анимация: красим иконку в зелёный и чуть ждём
            int green = context.getResources().getColor(android.R.color.holo_green_dark);
            holder.imageDone.setColorFilter(green, PorterDuff.Mode.SRC_IN);
            holder.imageDone.postDelayed(() -> {
                if (actionListener != null) {
                    actionListener.onTaskCompleted(task);
                }
            }, 200);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    private String getStatusText(int status) {
        switch (status) {
            case Task.STATUS_IN_PROGRESS:
                return context.getString(R.string.status_in_progress);
            case Task.STATUS_DONE:
                return context.getString(R.string.status_done);
            case Task.STATUS_NOT_STARTED:
            default:
                return context.getString(R.string.status_not_started);
        }
    }

    public interface TaskActionListener {
        void onTaskCompleted(Task task);
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle;
        TextView textStatus;
        ChipGroup chipGroupTags;
        ImageView imageDone;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textStatus = itemView.findViewById(R.id.textStatus);
            chipGroupTags = itemView.findViewById(R.id.chipGroupTags);
            imageDone = itemView.findViewById(R.id.imageDone);
        }
    }
}

