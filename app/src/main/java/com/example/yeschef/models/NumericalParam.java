package com.example.yeschef.models;

public class NumericalParam {
    public Compare Compare;
    public float FilterValue;

    public NumericalParam(Compare compare, float filterValue) {
        Compare = compare;
        FilterValue = filterValue;
    }
    public boolean IsValid(int recipeValue) {
        if (FilterValue <= 0)
            return true;
        return Compare.compare(recipeValue, FilterValue);
    }
}
