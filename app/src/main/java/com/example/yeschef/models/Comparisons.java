package com.example.yeschef.models;

public class Comparisons {
    public static final Compare LESS_THAN = (a, b) -> a < b;
    public static final Compare LESS_THAN_EQUALS = (a, b) -> a <= b;
    public static final Compare EQUALS = (a, b) -> a == b;
    public static final Compare GREATER_THAN_EQUALS = (a, b) -> a >= b;
    public static final Compare GREATER_THAN = (a, b) -> a > b;
}
