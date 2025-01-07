package com.example.yeschef.models;

import android.util.Log;
import android.net.Uri;

import com.google.gson.Gson;

import java.util.ArrayList;

public class Recipe {
    private String recipeTitle;
    private int cal;
    private int protein;
    private String image;
    private int servingSize;
    private String description;
    private boolean isVegetarian = false;
    private boolean isSugarFree = false;
    private boolean isGlutenFree = false;
    private ArrayList<String> ingredients;
    private ArrayList<String> directions;
    private static int count = 0;
    private int id = 0;

    //MealTime enumeration
    public enum MealTime {
        ANYTIME,
        BREAKFAST,
        LUNCH,
        DINNER
    }

    //Difficulty level enumeration
    public enum DifficultyLevel{
        EASY,
        MEDIUM,
        HARD
    }

    private MealTime mealTime;
    private DifficultyLevel difficultyLevel;

    public Recipe(String name, ArrayList<String> ingredients, int servingSize,
                  int cal, int protein, String description, ArrayList<String> directions,
                  MealTime mealTime, boolean isVegetarian, boolean isSugarFree, boolean isGlutenFree, DifficultyLevel difficultyLevel) {
        this.recipeTitle = name;
        this.ingredients = ingredients;
        this.directions = directions;
        this.servingSize = servingSize;
        this.cal = cal;
        this.protein = protein;
        this.description = description;
        this.mealTime = mealTime;
        this.isVegetarian = isVegetarian;
        this.isSugarFree = isSugarFree;
        this.isGlutenFree = isGlutenFree;
        this.difficultyLevel = difficultyLevel;
        id = count++;
    }
    public Recipe() {
        this.recipeTitle = "";
        this.servingSize = 0;
        this.description = "";
        this.cal = protein = 0;
        this.ingredients = new ArrayList<>();  // Initialize as empty list
        this.directions = new ArrayList<>();
        this.mealTime = MealTime.ANYTIME;
        this.difficultyLevel = DifficultyLevel.EASY;
        this.image = "";
    }

    // Getters
    public String getTitle() { return recipeTitle; }
    public String getDescription() { return description; }
    public ArrayList<String> getIngredients() { return ingredients; }
    public ArrayList<String> getDirections() { return directions; }
    public int getServingSize() { return servingSize; }
    public int getCal() { return cal; }
    public int getProtein() { return protein; }
    public boolean getIsVegetarian() { return isVegetarian; }
    public boolean getIsGlutenFree() { return isGlutenFree; }
    public boolean getIsSugarFree()  { return isSugarFree; }
    public Uri getImage() { return Uri.parse(image); }
    public MealTime getMealTime() { return mealTime; }
    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public int getId() { return id; }

    // Setters
    public void setTitle(String recipeTitle) { this.recipeTitle = recipeTitle; }
    public void setIngredients(ArrayList<String> ingredients) { this.ingredients = ingredients; }
    public void setDirections(ArrayList<String> directions) { this.directions = directions; }
    public void setServingSize(int servingSize) { this.servingSize = servingSize; }
    public void setDescription(String description) { this.description = description; }
    public void setCal(int cal) { this.cal = cal; }
    public void setProtein(int protein) { this.protein = protein; }
    public void setMealTime(MealTime mealTime) { this.mealTime = mealTime; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public void setVegetarian(boolean isVegetarian) { this.isVegetarian = isVegetarian; }
    public void setGlutenFree(boolean isGlutenFree) { this.isGlutenFree = isGlutenFree; }
    public void setSugarFree(boolean isSugarFree) { this.isSugarFree = isSugarFree; }
    public void setImage(String uri) {
        if (uri == null)
        {
            Log.e("URI", "NULL");
        }
        else {

            Log.e("URI", uri);
            image = uri;
        }
    }
    public void setId(int id) {
        this.id = id;
        // Ensure the static count keeps up with the highest ID for when app is shut off
        if (id >= count) {
            count = id + 1;
        }
    }

   //toString displays the recipe
   @Override
   public String toString() {
       return "Recipe Title: " + recipeTitle + '\n' +
               "Description: " + description + '\n' +
               "Meal Time: " + mealTime + '\n' +
               "Difficulty Level: " + difficultyLevel + '\n' +
               "Serving Size: " + servingSize + '\n' +
               "Calories: " + cal + '\n' +
               "Protein: " + protein + '\n' +
               "Vegetarian: " + formatBool(isVegetarian) + '\n' +
               "Gluten Free: " + formatBool(isGlutenFree) + '\n' +
               "Sugar Free: " + formatBool(isSugarFree) + '\n' +
               "Image: \n" + image + '\n' +
               "Ingredients: \n" + formatIngredients() + '\n' +
               "Directions: \n" + formatDirections();
    }

    //formatter functions for booleans
    private String formatBool(boolean isSomething) {
      String answer;
        if (isSomething) {
            answer = "yes";
        } else {
            answer = "no";
        }
        return answer;
    }

    //formatter functions for our lists
    private String formatIngredients() {
        StringBuilder sb = new StringBuilder();
        for (String ingredient : ingredients) {
            sb.append("- ").append(ingredient).append('\n');
        }
        return sb.toString();
    }

    private String formatDirections() {
        StringBuilder sb = new StringBuilder();
        int stepNumber = 1;
        for (String direction : directions) {
            sb.append(stepNumber++).append(". ").append(direction).append('\n');
        }
        return sb.toString();
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this); // Converts the Recipe object to JSON string
    }
}