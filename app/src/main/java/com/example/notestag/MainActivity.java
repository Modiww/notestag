package com.example.notestag;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CREATE_TASK = 1;

    private static final int SORT_BY_DATE = 0;
    private static final int SORT_BY_IMPORTANCE = 1;
    private static final int SORT_BY_URGENCY = 2;
    private static final int SORT_BY_TITLE = 3;
    private static final int SORT_BY_SPHERE = 4;

    private final List<Task> allTasks = new ArrayList<>();
    private final List<Task> visibleTasks = new ArrayList<>();
    private final List<String> activeFilterTags = new ArrayList<>();
    private int currentSort = SORT_BY_DATE;

    private TaskAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.main_title);

        RecyclerView recyclerView = findViewById(R.id.recyclerTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(this, visibleTasks, task -> {
            allTasks.remove(task);
            applyFiltersAndSort();
        });
        recyclerView.setAdapter(adapter);

        Button buttonFilter = findViewById(R.id.buttonFilter);
        Button buttonSort = findViewById(R.id.buttonSort);

        buttonFilter.setOnClickListener(v -> openFilterDialog());
        buttonSort.setOnClickListener(v -> openSortDialog());

        // свайп вправо с красным крестиком для удаления
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < visibleTasks.size()) {
                    Task task = visibleTasks.get(position);
                    allTasks.remove(task);
                    applyFiltersAndSort();
                }
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);

        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(v -> openCreateTask());
    }

    private void openCreateTask() {
        Intent intent = new Intent(this, CreateTaskActivity.class);
        startActivityForResult(intent, REQUEST_CREATE_TASK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CREATE_TASK && resultCode == RESULT_OK && data != null) {
            Task newTask = (Task) data.getSerializableExtra(CreateTaskActivity.EXTRA_NEW_TASK);
            if (newTask != null) {
                allTasks.add(newTask);
                applyFiltersAndSort();
            }
        }
    }

    private void applyFiltersAndSort() {
        visibleTasks.clear();
        // фильтрация по выбранным тегам
        for (Task task : allTasks) {
            if (matchesFilter(task)) {
                visibleTasks.add(task);
            }
        }

        // сортировка
        Collections.sort(visibleTasks, getComparator());
        adapter.notifyDataSetChanged();
    }

    private boolean matchesFilter(Task task) {
        if (activeFilterTags.isEmpty()) {
            return true;
        }
        List<String> tags = task.getTags();
        for (String filterTag : activeFilterTags) {
            if (!tags.contains(filterTag)) {
                return false;
            }
        }
        return true;
    }

    private Comparator<Task> getComparator() {
        return (t1, t2) -> {
            switch (currentSort) {
                case SORT_BY_IMPORTANCE:
                    return Integer.compare(getImportanceLevel(t2), getImportanceLevel(t1)); // по убыванию
                case SORT_BY_URGENCY:
                    return Integer.compare(getUrgencyLevel(t2), getUrgencyLevel(t1)); // по убыванию
                case SORT_BY_TITLE:
                    return t1.getTitle().compareToIgnoreCase(t2.getTitle());
                case SORT_BY_SPHERE:
                    return getSphere(t1).compareToIgnoreCase(getSphere(t2));
                case SORT_BY_DATE:
                default:
                    return Long.compare(t2.getCreatedAt(), t1.getCreatedAt()); // новые сверху
            }
        };
    }

    private int getImportanceLevel(Task task) {
        // Низкая / Средняя / Высокая / Критическая
        int level = -1;
        for (String tag : task.getTags()) {
            if ("Низкая".equalsIgnoreCase(tag)) level = Math.max(level, 0);
            if ("Средняя".equalsIgnoreCase(tag)) level = Math.max(level, 1);
            if ("Высокая".equalsIgnoreCase(tag)) level = Math.max(level, 2);
            if ("Критическая".equalsIgnoreCase(tag)) level = Math.max(level, 3);
        }
        return level;
    }

    private int getUrgencyLevel(Task task) {
        // Не срочно / Срочно / Горит
        int level = -1;
        for (String tag : task.getTags()) {
            if ("Не срочно".equalsIgnoreCase(tag)) level = Math.max(level, 0);
            if ("Срочно".equalsIgnoreCase(tag)) level = Math.max(level, 1);
            if ("Горит".equalsIgnoreCase(tag)) level = Math.max(level, 2);
        }
        return level;
    }

    private String getSphere(Task task) {
        for (String tag : task.getTags()) {
            if ("Работа".equalsIgnoreCase(tag)
                    || "Личное".equalsIgnoreCase(tag)
                    || "Дом".equalsIgnoreCase(tag)
                    || "Покупки".equalsIgnoreCase(tag)
                    || "Здоровье".equalsIgnoreCase(tag)
                    || "Финансы".equalsIgnoreCase(tag)
                    || "Обучение".equalsIgnoreCase(tag)) {
                return tag;
            }
        }
        return "";
    }

    private void openFilterDialog() {
        // Собираем список всех тегов из задач
        Set<String> allTagsSet = new HashSet<>();
        for (Task task : allTasks) {
            allTagsSet.addAll(task.getTags());
        }
        final String[] allTags = allTagsSet.toArray(new String[0]);

        boolean[] checkedItems = new boolean[allTags.length];
        for (int i = 0; i < allTags.length; i++) {
            checkedItems[i] = activeFilterTags.contains(allTags[i]);
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_filter_title)
                .setMultiChoiceItems(allTags, checkedItems, (dialog, which, isChecked) -> {
                    String tag = allTags[which];
                    if (isChecked) {
                        if (!activeFilterTags.contains(tag)) {
                            activeFilterTags.add(tag);
                        }
                    } else {
                        activeFilterTags.remove(tag);
                    }
                })
                .setPositiveButton(android.R.string.ok, (dialog, which) -> applyFiltersAndSort())
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.clear_filters, (dialog, which) -> {
                    activeFilterTags.clear();
                    applyFiltersAndSort();
                })
                .show();
    }

    private void openSortDialog() {
        final String[] options = new String[]{
                getString(R.string.sort_by_date),
                getString(R.string.sort_by_importance),
                getString(R.string.sort_by_urgency),
                getString(R.string.sort_by_title),
                getString(R.string.sort_by_sphere)
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_sort_title)
                .setSingleChoiceItems(options, currentSort, (dialog, which) -> currentSort = which)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> applyFiltersAndSort())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}

