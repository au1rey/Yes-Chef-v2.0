package com.example.yeschef.utils;

import android.content.Context;
import android.util.Log;

import com.example.yeschef.models.Recipe;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class JsonUtils {

    public static void saveRecipeMapToJson(Context context, Map<Integer, Recipe> recipeMap, String fileName) {
        Gson gson = new Gson();
        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
             OutputStreamWriter osw = new OutputStreamWriter(fos)) {

            gson.toJson(recipeMap, osw); // Convert the map to JSON and save
            osw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<Integer, Recipe> loadRecipeMapFromJson(Context context, String fileName) {
        Gson gson = new Gson();
        Map<Integer, Recipe> recipeMap = new HashMap<>();

        File file = new File(context.getFilesDir(), fileName);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file);
                 InputStreamReader isr = new InputStreamReader(fis);
                 BufferedReader reader = new BufferedReader(isr)) {

                Type type = new TypeToken<Map<Integer, Recipe>>() {}.getType();
                recipeMap = gson.fromJson(reader, type); // Convert JSON to Map
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return recipeMap;
    }

}
