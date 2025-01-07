package com.example.yeschef.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.yeschef.R;

import java.util.ArrayList;
import java.util.Arrays;

public class BreakfastMealsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_breakfast_meals, container, false);

        // Sample data for breakfast meals
        ArrayList<String> breakfastMeals = new ArrayList<>(Arrays.asList(
                "Pancakes", "Omelette", "Smoothie", "Toast", "Fruit Salad"));

        // Find the ListView and set up the adapter
        ListView listView = view.findViewById(R.id.breakfast_meals_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireActivity(), // Use requireActivity() for better context handling
                android.R.layout.simple_list_item_1,
                breakfastMeals
        );
        listView.setAdapter(adapter);

        return view;
    }
}
