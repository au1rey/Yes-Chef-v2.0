    package com.example.yeschef.fragments;

    import android.graphics.Paint;
    import android.os.Bundle;
    import android.text.Editable;
    import android.text.TextWatcher;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.view.animation.AlphaAnimation;
    import android.view.animation.Animation;
    import android.view.animation.ScaleAnimation;
    import android.widget.ImageButton;
    import android.widget.LinearLayout;

    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AlertDialog;
    import androidx.fragment.app.Fragment;
    import com.example.yeschef.R;
    import com.example.yeschef.models.ShoppingItem;
    import com.example.yeschef.utils.ShoppingListStorage;
    import com.google.android.material.textfield.TextInputEditText;

    import java.util.ArrayList;
    import java.util.List;

    public class ListFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_list, container, false);

            ImageButton addTaskButton  = view.findViewById(R.id.addTaskButton);
            ImageButton clearButton    = view.findViewById(R.id.clear_button);
            LinearLayout taskContainer = view.findViewById(R.id.taskContainer);

            addTaskButton.setOnClickListener(v -> addTaskView(LayoutInflater.from(getContext()), taskContainer, true, null));
            clearButton.setOnClickListener(v -> {
                new AlertDialog.Builder(view.getContext())
                    .setTitle("Clear Shopping List")
                    .setMessage("Are you sure you want to clear the entire shopping list?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        taskContainer.removeAllViews();
                        updateStorage();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
            });

            ShoppingListStorage storage = new ShoppingListStorage(requireContext());
            List<ShoppingItem> savedItems = storage.loadShoppingList();

            taskContainer.removeAllViews();

            // Load saved items into the task container
            for (ShoppingItem item : savedItems)
                addTaskView(LayoutInflater.from(getContext()), taskContainer, false, item.getName());

            // Workaround: Reset texts after a slight delay
            new android.os.Handler().postDelayed(() -> {
                for (int i = 0; i < savedItems.size(); i++) {
                    if (i < taskContainer.getChildCount()) {
                        View taskView = taskContainer.getChildAt(i);
                        TextInputEditText taskInput = taskView.findViewById(R.id.item_task_input);
                        if (taskInput != null)
                            taskInput.setText(savedItems.get(i).getName());
                    }
                }
            }, 250); //

            return view;
        }

        private void updateStorage() {
            LinearLayout container = getView().findViewById(R.id.taskContainer);
            List<ShoppingItem> shoppingItems = new ArrayList<>();

            for (int i = 0; i < container.getChildCount(); i++) {
                View child = container.getChildAt(i);
                TextInputEditText input = child.findViewById(R.id.item_task_input);

                if (input != null) {
                    String text = input.getText().toString().trim();

                    // Skip if the item is checked-off or empty
                    if (!text.isEmpty() && (input.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) == 0) {
                        shoppingItems.add(new ShoppingItem(text));
                    }
                }
            }

            ShoppingListStorage storage = new ShoppingListStorage(requireContext());
            storage.saveShoppingList(shoppingItems);
        }


        public void addTaskView(LayoutInflater inflater, ViewGroup taskContainer, boolean useAnimations, @Nullable String prefilledText) {
            // Inflate a new task using item_task.xml
            View newTask = inflater.inflate(R.layout.item_task, taskContainer, false);

            TextInputEditText taskInput = newTask.findViewById(R.id.item_task_input);
            ImageButton checkTaskButton = newTask.findViewById(R.id.checkmarkButton);
            ImageButton deleteTaskButton = newTask.findViewById(R.id.deleteTaskButton);

            taskInput.setHint("New Item");

            // Safely set prefilled text
            if (prefilledText != null) {
                taskInput.setText(prefilledText);
            }

            // Attach a TextWatcher after setting the initial value
            taskInput.post(() -> {
                taskInput.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        updateStorage();
                    }
                });
            });

            if (useAnimations) {
                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setDuration(300);
                newTask.startAnimation(fadeIn);
            }

            checkTaskButton.setOnClickListener(v -> {
                if (taskInput.getText() != null && taskInput.getText().length() > 0) {
                    taskInput.setPaintFlags(taskInput.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    taskInput.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    taskContainer.removeView(newTask);
                    taskContainer.addView(newTask); // Move to the bottom
                }
                updateStorage();
            });

            deleteTaskButton.setOnClickListener(v -> {
                ScaleAnimation scaleDown = new ScaleAnimation(
                        1.0f, 1.0f, 1.0f, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.0f
                );
                scaleDown.setDuration(300); // Duration of the animation in milliseconds
                scaleDown.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        taskContainer.removeView(newTask);
                        updateStorage(); // Refresh storage after item deletion
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                newTask.startAnimation(scaleDown);
            });

            taskContainer.addView(newTask, 0);
        }
    }
