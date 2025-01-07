package com.example.yeschef.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import androidx.core.content.ContextCompat;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;  // Add this import statement
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.yeschef.R;
import com.example.yeschef.models.Recipe;
import com.example.yeschef.utils.JsonUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddFragment extends Fragment {

    private static final String KEY_INGREDIENTS_LIST = "ingredients_list";
    private static final String KEY_DIRECTIONS_LIST = "directions_list";

    private ArrayList<String> ingredientsList;
    private ArrayList<String> directionsList;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private ImageButton clickedButton;
    private int clickedIndex;
    private LinearLayout categoryButtonsContainer;

    private LinearLayout directionsContainer;
    private LinearLayout ingredientsContainer;
    private boolean isAnimating = false;
    private Spinner mealTimeSpinner;
    private Spinner difficultySpinner;
    private EditText titleInput;
    private EditText descriptionInput;
    private ImageButton imageAdder;
    private EditText servingSizeInput;
    private EditText caloriesInput;
    private EditText proteinInput;
    private Chip optionVegetarian;
    private Chip optionGlutenFree;
    private Chip optionSugarFree;
    private String image;
    private Button saveButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        // Cache views
        titleInput           = view.findViewById(R.id.meal_title);
        descriptionInput     = view.findViewById(R.id.meal_description);
        servingSizeInput     = view.findViewById(R.id.serving_size_input);
        caloriesInput        = view.findViewById(R.id.calories_input);
        proteinInput         = view.findViewById(R.id.protein_input);
        mealTimeSpinner      = view.findViewById(R.id.spinner1);
        difficultySpinner    = view.findViewById(R.id.difficulty_spinner);
        imageAdder           = view.findViewById(R.id.image_add);
        ingredientsContainer = view.findViewById(R.id.ingredients_container);
        directionsContainer  = view.findViewById(R.id.directions_container);

        // Set up labels and their colors
        RelativeLayout ingredientsLabel = view.findViewById(R.id.ingredients_label);
        RelativeLayout directionsLabel = view.findViewById(R.id.directions_label);

        // Get header colors
        int ingredientsColor = ContextCompat.getColor(view.getContext(), R.color.ingredient);
        int directionsColor = ContextCompat.getColor(view.getContext(), R.color.step);

        // Setup basic text fields
        setHintText(view, R.id.meal_title, "Meal Title");
        setHintText(view, R.id.meal_description, "Description");

        // Disable click status for labels
        ingredientsLabel.setClickable(false);
        directionsLabel.setClickable(false);

        initializeGalleryLauncher(view);
        initializeImageAdder();

        // Initialize lists for ingredients and directions
        if (savedInstanceState != null) {
            // Restore saved lists
            ingredientsList = savedInstanceState.getStringArrayList(KEY_INGREDIENTS_LIST);
            directionsList = savedInstanceState.getStringArrayList(KEY_DIRECTIONS_LIST);

            // Populate ingredients
            if (ingredientsList != null) {
                for (String ingredient : ingredientsList) {
                    addNewItem(ingredientsContainer, "Ingredient(s)", ingredientsColor, directionsContainer, ingredient, true);
                }
            }

            // Populate directions
            if (directionsList != null) {
                for (String direction : directionsList) {
                    addNewItem(directionsContainer, "Step(s)", directionsColor, ingredientsContainer, direction, true);
                }
            }
        } else {
            // Initialize empty lists
            ingredientsList = new ArrayList<>();
            directionsList = new ArrayList<>();
        }

        // Set up the meal time spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                R.array.mealtime_options, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealTimeSpinner.setAdapter(adapter);

        // Set up the difficulty spinner
        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(
                view.getContext(), R.array.difficulty_levels, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(difficultyAdapter);

        // Set click listeners for toggling visibility
        ingredientsLabel.setOnClickListener(v -> toggleContainerVisibility(ingredientsContainer));
        directionsLabel.setOnClickListener(v -> toggleContainerVisibility(directionsContainer));

        // Add buttons for adding new items
        addButtonListeners(view, ingredientsContainer, directionsContainer, ingredientsColor, directionsColor);

        // Add buttons for dietary options
        optionVegetarian = view.findViewById(R.id.option_vegetarian);
        optionGlutenFree = view.findViewById(R.id.option_gluten_free);
        optionSugarFree = view.findViewById(R.id.option_sugar_free);

        // Add save button click listener
        ImageButton saveButton = view.findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            if (!validateInput(view)) {
                return;
            }

            onSaveClick();
            // Show alert for save confirmation
            new AlertDialog.Builder(view.getContext())
                    .setTitle("Recipe Saved")
                    .setMessage("The " + (titleInput.getText().toString().isEmpty() ?
                            "untitled" : titleInput.getText().toString()) + " recipe has been saved." +
                            "\nIt is safe to clear this page.")
                    .setPositiveButton("Okay", (dialog, which) -> dialog.dismiss())
                    .show();
            });

        // Add clear button click listener
        ImageButton clearButton = view.findViewById(R.id.clear_button);
        clearButton.setOnClickListener(v -> {
            // Create an AlertDialog builder
            new AlertDialog.Builder(view.getContext())
                    .setTitle("Clear Recipe")
                    .setMessage("Are you sure you want to clear all fields?")
                    .setPositiveButton("Clear", (dialog, which) -> onClearClick())
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        updateIngredientsAndDirectionsViews();

        Bundle args = getArguments();

        // When loading from recipe browse, populate the edit page with the fields from the recipe
        // that is being loaded
        if (args != null) {
            String recipeName = args.getString("recipeName");
            String recipeImageUri = args.getString("recipeImageUri");

            // Populate the UI
            if (args.containsKey("recipeTitle"))
                titleInput.setText(args.getString("recipeTitle"));
            if (args.containsKey("recipeDescription"))
                descriptionInput.setText(args.getString("recipeDescription"));

            if (args.containsKey("servingSize"))
                servingSizeInput.setText(String.valueOf(args.getInt("servingSize")));
            if (args.containsKey("calories"))
                caloriesInput.setText(String.valueOf(args.getInt("calories")));
            if (args.containsKey("protein"))
                proteinInput.setText(String.valueOf(args.getInt("protein")));
            if (args.containsKey("difficulty"))
                difficultySpinner.setSelection(args.getInt("difficulty"));
            if (args.containsKey("mealtime"))
                mealTimeSpinner.setSelection(args.getInt("mealtime"));

            if (args.containsKey("isVegetarian"))
                optionVegetarian.setChecked(args.getBoolean("isVegetarian"));
            if (args.containsKey("isGlutenFree"))
                optionGlutenFree.setChecked(args.getBoolean("isGlutenFree"));
            if (args.containsKey("isSugarFree"))
                optionSugarFree.setChecked(args.getBoolean("isSugarFree"));

            if (args.containsKey("image"))
                if (!args.getString("image").isEmpty())
                    SetImage(args.getString("image"));
            if (args.containsKey("ingredients"))
                for (String ingredient : args.getStringArrayList("ingredients"))
                    addNewItem(ingredientsContainer, "Ingredient(s)", ingredientsColor, directionsContainer, ingredient, false);
            if (args.containsKey("directions"))
                for (String direction : args.getStringArrayList("directions"))
                    addNewItem(directionsContainer, "Step(s)", directionsColor, null, direction, false);
        }

        return view;
    }
    private boolean validateInput(View view) {
        TextInputLayout mealTitleLayout = view.findViewById(R.id.meal_title_layout);
        TextInputEditText mealTitleEditText = view.findViewById(R.id.meal_title);

        // Get the text from the EditText
        String mealTitle = mealTitleEditText.getText().toString().trim();

        // Check if the field is empty
        if (mealTitle.isEmpty()) {
            mealTitleLayout.setError("This field is required");
            return false;
        } else {
            mealTitleLayout.setError(null); // Clear the error
            return true;
        }
    }
    // Sets the image to the specified URI
    private void SetImage(String uri)
    {
        imageAdder.clearColorFilter();
        imageAdder.setColorFilter(null);
        imageAdder.setImageURI(Uri.parse(uri)); // Load from app storage
        imageAdder.setPadding(0, 0, 0, 0);
    }

    // reset UI method
    private void resetFields() {

        EditText servingSizeInput = requireView().findViewById(R.id.serving_size_input);
        View caloriesView = getView().findViewById(R.id.calories_input);
        View proteinView = getView().findViewById(R.id.protein_input);

        // Reset dietary option Chips to unselected state
        ChipGroup dietaryOptionsGroup = requireView().findViewById(R.id.dietary_options_group);
        dietaryOptionsGroup.clearCheck();

        titleInput.setText("");     // Clear the meal title
        descriptionInput.setText("");     // Clear the meal description

        // Clear serving size
        servingSizeInput.setText("");

        if (caloriesView instanceof TextView) {
            ((TextView) caloriesView).setText("");
        }
        if (proteinView instanceof TextView) {
            ((TextView) proteinView).setText("");
        }

        Spinner mealTimeSpinner = getView().findViewById(R.id.spinner1);
        mealTimeSpinner.setSelection(0);

        Spinner difficultySpinner = getView().findViewById(R.id.difficulty_spinner);
        difficultySpinner.setSelection(0);

        updateIngredientsAndDirectionsViews();
    }
    private void updateIngredientsAndDirectionsViews() {
        while (ingredientsContainer.getChildCount() > 1) { // Keep the header (assume header is the first child)
            ingredientsContainer.removeViewAt(1);    // Remove all but the header
        }

        while (directionsContainer.getChildCount() > 1) { // Keep the header (assume header is the first child)
            directionsContainer.removeViewAt(1);    // Remove all but the header
        }
    }
    private void addButtonListeners(View view, LinearLayout ingredientsContainer, LinearLayout directionsContainer, int ingredientsColor, int directionsColor) {
        ImageButton addIngredientButton = view.findViewById(R.id.add_ingredient_button);
        ImageButton addDirectionButton = view.findViewById(R.id.add_direction_button);

        addIngredientButton.setOnClickListener(v -> addNewItem(ingredientsContainer, "Ingredient(s)", ingredientsColor, directionsContainer, true));
        addDirectionButton.setOnClickListener(v -> addNewItem(directionsContainer, "Step(s)", directionsColor, null, true));
    }

    private void toggleContainerVisibility(final LinearLayout container) {
        // Logic for toggling visibility
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(KEY_INGREDIENTS_LIST, new ArrayList<>(ingredientsList));
        outState.putStringArrayList(KEY_DIRECTIONS_LIST, new ArrayList<>(directionsList));
    }
    private void onSaveClick() {
        // Check recipe map storage
        Map<Integer, Recipe> loadedRecipeMap;
        loadedRecipeMap = JsonUtils.loadRecipeMapFromJson(requireContext(), "recipes.json");

        if (loadedRecipeMap == null) {
            loadedRecipeMap = new HashMap<>(); // Initialize if the file is empty or doesn't exist
            Log.d("Loaded Test", "Recipe map was null; initialized new map.");        }

        // Log the loaded map
        Log.d("Loaded Test", "Loaded recipe map content before save: " + loadedRecipeMap.toString());

        // Check if we're editing an existing recipe
        int recipeId = -1; // Default ID indicating new recipe
        Log.d("MyTest", "Is Null: " + getArguments());
        if (getArguments() != null)
            Log.d("MyTest", "Contains Key: " + getArguments().containsKey("recipeId"));
        if (getArguments() != null && getArguments().containsKey("recipeId")) {
            recipeId = getArguments().getInt("recipeId"); // Retrieve the recipeId from arguments
        }

        // Create a new Recipe object
        Recipe recipe;

        if (recipeId != -1 && loadedRecipeMap.containsKey(recipeId)) {
            // If the recipeId exists, fetch the existing recipe for update
            recipe = loadedRecipeMap.get(recipeId);
            Log.d("MyTest", "Found");
        } else {
            // Create a new Recipe object if not editing
            recipe = new Recipe();
            recipeId = loadedRecipeMap.size() + 1; // Assign a new ID
            recipe.setId(recipeId);
            Log.d("MyTest", "Not Found");
        }

        // Retrieve input from UI components
        String title              = titleInput.getText().toString();
        String description        = descriptionInput.getText().toString();
        String servingSizeStr     = servingSizeInput.getText().toString();
        String caloriesStr        = caloriesInput.getText().toString();
        String proteinStr         = proteinInput.getText().toString();
        String mealTimeStr        = mealTimeSpinner.getSelectedItem().toString();
        String difficultyLevelStr = difficultySpinner.getSelectedItem().toString();

        // Convert mealTimeStr to MealTime enum
        Recipe.MealTime mealTime;
        switch (mealTimeStr) {
            case "Breakfast":
                mealTime = Recipe.MealTime.BREAKFAST;
                break;
            case "Lunch":
                mealTime = Recipe.MealTime.LUNCH;
                break;
            case "Dinner":
                mealTime = Recipe.MealTime.DINNER;
                break;
            default:
                mealTime = Recipe.MealTime.ANYTIME;
                break;
        }

        // Convert Difficulty Level string to Difficulty Level enum
        Recipe.DifficultyLevel difficultyLevel;
        switch (difficultyLevelStr) {
            case "Medium":
                difficultyLevel = Recipe.DifficultyLevel.MEDIUM;
                break;
            case "Hard":
                difficultyLevel = Recipe.DifficultyLevel.HARD;
                break;
            default:
                difficultyLevel = Recipe.DifficultyLevel.EASY;
                break;
        }

        // Parse numeric inputs (with default values if empty)
        int servingSize = servingSizeStr.isEmpty() ? 1 : Integer.parseInt(servingSizeStr);
        int calories = caloriesStr.isEmpty() ? 0 : Integer.parseInt(caloriesStr);
        int protein = proteinStr.isEmpty() ? 0 : Integer.parseInt(proteinStr);

        // Set user input to the Recipe object
        recipe.setTitle(title);
        recipe.setDescription(description);
        recipe.setServingSize(servingSize);
        recipe.setCal(calories);
        recipe.setProtein(protein);
        recipe.setMealTime(mealTime);
        recipe.setDifficultyLevel(difficultyLevel);

        //Retrieve the ingredients
        ingredientsList.clear();
        for (int i = 0; i < ingredientsContainer.getChildCount(); i++) {
            View child = ingredientsContainer.getChildAt(i);
            TextInputEditText inputField = child.findViewById(R.id.ingredient_step_input);
            if (inputField != null) {
                String ingredientText = inputField.getText().toString();
                if (!ingredientText.isEmpty()) {
                    ingredientsList.add(ingredientText);
                    Log.d("OnSaveClick", "Ingredient added: " + ingredientText); // Log added ingredient
                }
            }
        }

        // Retrieve the directions
        directionsList.clear();
        for (int i = 0; i < directionsContainer.getChildCount(); i++) {
            View child = directionsContainer.getChildAt(i);
            TextInputEditText inputField = child.findViewById(R.id.ingredient_step_input);
            if (inputField != null) {
                String directionText = inputField.getText().toString();
                if (!directionText.isEmpty()) {
                    directionsList.add(directionText);
                    Log.d("OnSaveClick", "Direction added: " + directionText); // Log added direction
                }
            }
        }

        recipe.setIngredients(ingredientsList);
        recipe.setDirections(directionsList);
        recipe.setVegetarian(optionVegetarian.isChecked());
        recipe.setGlutenFree(optionGlutenFree.isChecked());
        recipe.setSugarFree(optionSugarFree.isChecked());
        recipe.setImage(image);

        // Add recipe to map
        loadedRecipeMap.put(recipe.getId(), recipe);

        // Use the file name recipes.json
        String fileName = "recipes.json";
        JsonUtils.saveRecipeMapToJson(requireContext(), loadedRecipeMap, "recipes.json");

        Log.d("Loaded Test", "Loaded recipe map size: " + loadedRecipeMap.size());
        for (Map.Entry<Integer, Recipe> entry : loadedRecipeMap.entrySet()) {
            Log.d("Load Test", "Recipe ID: " + entry.getKey() + ", Title: " + entry.getValue().getTitle());
        }
    }
    private void onClearClick() {
        resetFields();
    }

    private void initializeGalleryLauncher(View view) {
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (!(result.getResultCode() == RESULT_OK && result.getData() != null))
                return;

            Uri imageUri = result.getData().getData();

            try {
                // Copy the selected image to app's private storage
                String copiedImagePath = copyImageToAppStorage(imageUri);

                // Update the ImageView with the copied image
                SetImage(copiedImagePath);

                // Save the path for persistent storage
                image = copiedImagePath;
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    private String copyImageToAppStorage(Uri sourceUri) throws IOException {
        // Get a unique file name
        String fileName = "image_" + System.currentTimeMillis() + ".jpg";

        // Get the app's private storage directory
        File storageDir = new File(requireContext().getFilesDir(), "images");
        if (!storageDir.exists()) {
            storageDir.mkdirs(); // Create the directory if it doesn't exist
        }

        // Create the destination file
        File destFile = new File(storageDir, fileName);

        // Open streams to copy the file
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(sourceUri);
             OutputStream outputStream = new FileOutputStream(destFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }

        // Return the absolute file path of the copied image
        return destFile.getAbsolutePath();
    }
    private void initializeImageAdder() {
        imageAdder.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });
    }

    private void setHintText(View view, int id, String text) {
        View titleItem = view.findViewById(id);
        TextInputEditText editText = titleItem.findViewById(R.id.ingredient_step_input);
        //editText.setHint(text);
    }
    private void updateStepNumbers(LinearLayout itemListContainer) {
        for (int i = 0; i < itemListContainer.getChildCount(); i++) {
            View stepItem = itemListContainer.getChildAt(i);
            TextView stepText = stepItem.findViewById(R.id.circle_text);  // Text inside the circle

            if (stepText != null) {
                stepText.setText(String.valueOf(i));  // Update the step number based on its position in the list
            } else {
                Log.e("TestCount", "TextView 'circle_text' is null in step item at index " + i);
            }
        }
    }
    private View addNewItem(LinearLayout itemListContainer, String hintText, int labelColor, LinearLayout otherContainer, boolean animate) {
        return addNewItem(itemListContainer, hintText, labelColor, otherContainer, null, animate);
    }
    private View addNewItem(LinearLayout itemListContainer, String hintText, int labelColor, LinearLayout otherContainer, String existingText, boolean animate) {
        if (animate) {
            if (isAnimating)
                return null;
            isAnimating = true;
        }

        // Inflate the item_list layout
        View newItem = getLayoutInflater().inflate(R.layout.item_list, itemListContainer, false);

        if (animate) {
            newItem.setAlpha(0f);
            newItem.setTranslationY(-30);

            // Animate the appearance of the new item
            newItem.animate()
                    .alpha(1f)
                    .translationY(0)
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> {
                        isAnimating = false;
                    })
                    .start();
        }

        // Set up the input field and hint
        TextInputEditText inputField = newItem.findViewById(R.id.ingredient_step_input);
        inputField.setHint(hintText); // Set the hint based on context

        if (existingText != null) {
            inputField.setText(existingText); // Pre-fill with existing text if provided
        }

        // Check if the item is being added to the directions container
        if (itemListContainer == directionsContainer) {
            FrameLayout stepFrame = newItem.findViewById(R.id.step_frame);
            stepFrame.setVisibility(View.VISIBLE); // Show step frame for directions
            new Handler().postDelayed(() -> updateStepNumbers(itemListContainer), 50);
        }

        // Set up the remove button
        ImageButton addRemoveButton = newItem.findViewById(R.id.remove_button);
        addRemoveButton.setOnClickListener(v -> {
            if (isAnimating) return;
            isAnimating = true;

            // Remove animation logic here if needed
            playDeleteAnimation(newItem, itemListContainer);
            playSlideDownAnimation(itemListContainer, itemListContainer.indexOfChild(newItem));

            if (itemListContainer == ingredientsContainer) {
                playSlideContainerAnimation(directionsContainer, newItem, -1);
            }
            if (otherContainer != null && itemListContainer.getChildCount() == 0) {
                addNewItem(itemListContainer, hintText, labelColor, otherContainer, true);
            }

            itemListContainer.removeView(newItem); // Remove the item from the container
            isAnimating = false; // Reset animation state
        });

        // Set the background color of the rectangle
        //View leftRectangle = newItem.findViewById(R.id.left_rectangle);
        //leftRectangle.setBackgroundColor(labelColor);

        // Add the new item to the container
        itemListContainer.addView(newItem);

        return newItem;
    }

    private void playDeleteAnimation(View itemView, LinearLayout itemListContainer) {
        itemView.animate()
                .alpha(0f)
                .translationY(-30)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    // This block will run after the animation ends
                    itemListContainer.removeView(itemView);  // Remove the view from the container
                    if (itemListContainer == directionsContainer) {
                        updateStepNumbers(itemListContainer);
                    }
                    isAnimating = false;
                })
                .start();
    }

    private void playSlideDownAnimation(LinearLayout itemListContainer, int deletedIndex) {
        for (int i = deletedIndex + 1; i < itemListContainer.getChildCount(); i++) {
            View item = itemListContainer.getChildAt(i);

            item.animate()
                    .translationYBy(-item.getHeight())
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> {
                        item.setTranslationY(0);
                    })
                    .start();
        }
    }

    private void playSlideContainerAnimation(LinearLayout itemListContainer, View itemView, int direction) {
        itemListContainer.animate()
                .translationYBy(direction * itemView.getHeight())
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    itemListContainer.setTranslationY(0);
                })
                .start();
    }
}

