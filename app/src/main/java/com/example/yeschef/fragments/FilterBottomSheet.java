package com.example.yeschef.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.yeschef.R;
import com.example.yeschef.models.Compare;
import com.example.yeschef.models.Comparisons;
import com.example.yeschef.models.FilterParams;
import com.example.yeschef.models.NumericalParam;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FilterBottomSheet extends BottomSheetDialogFragment {

    // Callback interface to send data back to the fragment
    public interface FilterCallback {
        void onFilterApplied(FilterParams filterParams);
    }

    private FilterCallback filterCallback;

    public void setFilterCallback(FilterCallback callback) {
        this.filterCallback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.filter_side_panel, container, false);

        Button applyFilterButton = view.findViewById(R.id.apply_filter_button);
        Button cancelFilterButton = view.findViewById(R.id.cancel_filter_button);

        // Handle "Apply" button click
        applyFilterButton.setOnClickListener(v -> {
            String descriptionStr = ((TextInputEditText) view.findViewById(R.id.filter_description_input)).getText().toString();
            List<String> descriptionTags = convertCommaSeparatedToList(descriptionStr);

            String servingSizeInequality = ((Spinner) view.findViewById(R.id.filter_serving_size_inequality_spinner)).getSelectedItem().toString();
            int servingSize = parseInteger(((EditText) view.findViewById(R.id.filter_serving_size_input)).getText().toString());
            NumericalParam servingSizeParam = MakeNumericalParam(servingSize, servingSizeInequality);

            String calorieInequality = ((Spinner) view.findViewById(R.id.filter_calorie_inequality_spinner)).getSelectedItem().toString();
            int calories = parseInteger(((EditText) view.findViewById(R.id.filter_calorie_input)).getText().toString());
            NumericalParam calorieParam = MakeNumericalParam(calories, calorieInequality);

            String proteinInequality = ((Spinner) view.findViewById(R.id.filter_protein_inequality_spinner)).getSelectedItem().toString();
            int protein = parseInteger(((EditText) view.findViewById(R.id.fitler_protien_input)).getText().toString());
            NumericalParam proteinParam = MakeNumericalParam(protein, proteinInequality);

            List<String> difficulty = getSelectedChips(view.findViewById(R.id.filter_difficulty_chip_group));
            List<String> mealtime = getSelectedChips(view.findViewById(R.id.filter_mealtime_chip_group));
            List<String> dietaryOptions = getSelectedChips(view.findViewById(R.id.chip_group));

            String ingredientsStr = ((TextInputEditText) view.findViewById(R.id.filter_ingredients_input)).getText().toString().toLowerCase();
            String directionsStr = ((TextInputEditText) view.findViewById(R.id.filter_directions_input)).getText().toString().toLowerCase();

            List<String> ingredients = convertCommaSeparatedToList(ingredientsStr);
            List<String> directions = convertCommaSeparatedToList(directionsStr);

            // Bundle the filter parameters
            FilterParams filterParams = new FilterParams(descriptionTags, servingSizeParam, calorieParam,
                    proteinParam, difficulty, mealtime, dietaryOptions, ingredients, directions);

            // Pass the filters back to the ScrollingFragment
            dismiss();
            filterCallback.onFilterApplied(filterParams);
        });

        // Handle "Cancel" button click
        cancelFilterButton.setOnClickListener(v -> dismiss());

        return view;
    }

    private List<String> convertCommaSeparatedToList(String input) {
        if (input == null || input.trim().isEmpty())
            return Collections.emptyList();
        return Arrays.asList(input.split("\\s*,\\s*"));
    }

    private NumericalParam MakeNumericalParam(int filterValue, String inequalityStr) {
       return new NumericalParam(GetCompare(inequalityStr), filterValue);
    }

    private Compare GetCompare(String inequalityStr) {
        switch (inequalityStr) {
            case "<":  return Comparisons.LESS_THAN;
            case "<=": return Comparisons.LESS_THAN_EQUALS;
            case "==": return Comparisons.EQUALS;
            case ">=": return Comparisons.GREATER_THAN_EQUALS;
            case ">":  return Comparisons.GREATER_THAN;
            default:   return Comparisons.EQUALS;
        }
    }

    private int parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0; // Default value for invalid input
        }
    }
    private List<String> getSelectedChips(ChipGroup chipGroup) {
        List<String> selectedChips = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedChips.add(chip.getText().toString().toLowerCase());
            }
        }

        return selectedChips;
    }

}
