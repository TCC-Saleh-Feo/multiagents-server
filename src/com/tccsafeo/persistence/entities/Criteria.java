package com.tccsafeo.persistence.entities;

public class Criteria {
    public String name;
    public Integer min;
    public Integer max;

    public Criteria() {
        super();
    }

    public Criteria(String name, Integer min, Integer max) {
        this.name = name;
        this.min = min;
        this.max = max;
    }

    @Override
    public String toString() {
        return "Criteria {" +
                "name = " + name +
                ", min = " + min +
                ", max = " + max +
                "}";
    }
}
