package com.example.notestag;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CreateTaskActivity extends AppCompatActivity {

    public static final String EXTRA_NEW_TASK = "extra_new_task";

    private TextInputEditText editTitle;
    private TextInputEditText editDescription;
    private TextInputEditText editCustomTagGroup;
    private TextInputEditText editCustomTagName;
    private LinearLayout layoutTagGroupsContainer;
    private ChipGroup chipGroupStatus;

    // Храним соответствие "название группы" -> ChipGroup с тегами
    private final Map<String, ChipGroup> tagGroups = new LinkedHashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);
        setTitle(R.string.create_task_title);

        editTitle = findViewById(R.id.editTitle);
        editDescription = findViewById(R.id.editDescription);
        editCustomTagGroup = findViewById(R.id.editCustomTagGroup);
        editCustomTagName = findViewById(R.id.editCustomTagName);
        layoutTagGroupsContainer = findViewById(R.id.layoutTagGroupsContainer);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        Button buttonSave = findViewById(R.id.buttonSave);
        Button buttonAddTag = findViewById(R.id.buttonAddTag);

        setupDefaultTagGroups();
        setupStatusChips();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask();
            }
        });

        buttonAddTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCustomTagFromInputs();
            }
        });
    }

    /**
     * Создаём преднастроенные группы и теги:
     * - Важность
     * - Срочность
     * - Сфера
     */
    private void setupDefaultTagGroups() {
        // Важность
        addTagToGroup("Важность", "Низкая");
        addTagToGroup("Важность", "Средняя");
        addTagToGroup("Важность", "Высокая");
        addTagToGroup("Важность", "Критическая");

        // Срочность
        addTagToGroup("Срочность", "Не срочно");
        addTagToGroup("Срочность", "Срочно");
        addTagToGroup("Срочность", "Горит");

        // Сфера
        addTagToGroup("Сфера", "Работа");
        addTagToGroup("Сфера", "Личное");
        addTagToGroup("Сфера", "Дом");
        addTagToGroup("Сфера", "Покупки");
        addTagToGroup("Сфера", "Здоровье");
        addTagToGroup("Сфера", "Финансы");
        addTagToGroup("Сфера", "Обучение");
    }

    /**
     * Добавляет тег в указанную группу. Если группы нет – создаёт заголовок и ChipGroup.
     */
    private void addTagToGroup(String groupName, String tagName) {
        if (groupName == null || groupName.trim().isEmpty()) {
            groupName = getString(R.string.group_without_name);
        }
        groupName = groupName.trim();

        ChipGroup chipGroup = tagGroups.get(groupName);
        if (chipGroup == null) {
            // Создаём заголовок группы
            TextView titleView = new TextView(this);
            titleView.setText(groupName);
            titleView.setTextSize(14f);
            titleView.setPadding(0, dpToPx(12), 0, dpToPx(4));
            layoutTagGroupsContainer.addView(titleView);

            // Создаём ChipGroup для этой группы
            chipGroup = new ChipGroup(this);
            chipGroup.setSingleSelection(false);
            layoutTagGroupsContainer.addView(chipGroup);

            tagGroups.put(groupName, chipGroup);
        }

        Chip chip = new Chip(this);
        chip.setText(tagName);
        chip.setCheckable(true);
        chipGroup.addView(chip);
    }

    private void addCustomTagFromInputs() {
        String groupName = editCustomTagGroup.getText() != null
                ? editCustomTagGroup.getText().toString().trim()
                : "";
        String tagName = editCustomTagName.getText() != null
                ? editCustomTagName.getText().toString().trim()
                : "";

        if (tagName.isEmpty()) {
            editCustomTagName.setError(getString(R.string.custom_tag_name_hint));
            return;
        }

        addTagToGroup(groupName, tagName);

        // Очищаем только название тега, группу можно оставить той же
        editCustomTagName.setText("");
    }

    private void setupStatusChips() {
        // Не начата
        Chip chipNotStarted = new Chip(this);
        chipNotStarted.setText(getString(R.string.status_not_started));
        chipNotStarted.setCheckable(true);
        chipGroupStatus.addView(chipNotStarted);

        // В процессе
        Chip chipInProgress = new Chip(this);
        chipInProgress.setText(getString(R.string.status_in_progress));
        chipInProgress.setCheckable(true);
        chipGroupStatus.addView(chipInProgress);

        // Готова
        Chip chipDone = new Chip(this);
        chipDone.setText(getString(R.string.status_done));
        chipDone.setCheckable(true);
        chipGroupStatus.addView(chipDone);

        // По умолчанию – "Не начата"
        chipGroupStatus.check(chipNotStarted.getId());
    }

    private void saveTask() {
        String title = editTitle.getText() != null ? editTitle.getText().toString().trim() : "";
        String description = editDescription.getText() != null ? editDescription.getText().toString().trim() : "";

        if (title.isEmpty()) {
            editTitle.setError(getString(R.string.task_title_hint));
            return;
        }

        List<String> selectedTags = new ArrayList<>();
        // Проходим по всем группам и собираем отмеченные теги
        for (ChipGroup group : tagGroups.values()) {
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = group.getChildAt(i);
                if (child instanceof Chip) {
                    Chip chip = (Chip) child;
                    if (chip.isChecked()) {
                        selectedTags.add(chip.getText().toString());
                    }
                }
            }
        }

        int status = Task.STATUS_NOT_STARTED;
        int checkedId = chipGroupStatus.getCheckedChipId();
        if (checkedId != View.NO_ID) {
            Chip checkedChip = chipGroupStatus.findViewById(checkedId);
            if (checkedChip != null) {
                String text = checkedChip.getText().toString();
                if (text.equals(getString(R.string.status_in_progress))) {
                    status = Task.STATUS_IN_PROGRESS;
                } else if (text.equals(getString(R.string.status_done))) {
                    status = Task.STATUS_DONE;
                } else {
                    status = Task.STATUS_NOT_STARTED;
                }
            }
        }

        Task task = new Task(title, description, selectedTags);
        task.setStatus(status);
        Intent result = new Intent();
        result.putExtra(EXTRA_NEW_TASK, task);
        setResult(RESULT_OK, result);
        finish();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}

