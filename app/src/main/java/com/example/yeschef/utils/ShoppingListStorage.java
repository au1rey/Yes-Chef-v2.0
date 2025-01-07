package com.example.yeschef.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.yeschef.models.ShoppingItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ShoppingListStorage {

    private static final String PREF_NAME = "shopping_list_pref";
    private static final String TASKS_KEY = "tasks_key";

    private final SharedPreferences sharedPreferences;

    public ShoppingListStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveShoppingList(List<ShoppingItem> shoppingList) {
        Gson gson = new Gson();
        String json = gson.toJson(shoppingList);
        sharedPreferences.edit().putString(TASKS_KEY, json).apply();
    }

    public List<ShoppingItem> loadShoppingList() {
        String json = sharedPreferences.getString(TASKS_KEY, null);
        if (json == null) {
            return new ArrayList<>(); // Return empty list if no data is saved
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<ShoppingItem>>() {}.getType();
        return gson.fromJson(json, type);
    }
}

