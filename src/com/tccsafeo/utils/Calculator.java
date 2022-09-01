package com.tccsafeo.utils;

import java.util.ArrayList;

public class Calculator {
    public static Double getSum(ArrayList<Double> values) {
        Double sum = 0D;
        for (Integer i = 0; i < values.size(); i++) {
            sum += values.get(i);
        }
        return sum;
    }

    public static Double getAverage(ArrayList<Double> values) {
        try {
            return getSum(values) / values.size();
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("A null value was passed as an argument!");
        }
    }

    public static Double getStandardDeviation(ArrayList<Double> values) {
        Double avg = getAverage(values);
        Integer size = values.size();
        Double standardDeviation = 0D;
        for (Double value : values) {
            Double aux = value - avg;
            standardDeviation += aux * aux;
        }
        return Math.sqrt(standardDeviation / (size - 1));
    }

    public static Double normalize(Integer value, Integer min, Integer max) {
        return (double) (value - min) / (max - min);
    }
}
