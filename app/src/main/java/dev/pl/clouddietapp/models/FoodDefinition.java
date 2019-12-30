package dev.pl.clouddietapp.models;

import java.util.Objects;

public class FoodDefinition {
    private String id;
    private String name;
    private String unit;

    public FoodDefinition(String id, String name, String unit) {
        this.id = id;
        this.name = name;
        this.unit = unit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodDefinition that = (FoodDefinition) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, unit);
    }

    @Override
    public String toString() {
        return "FoodDefinition{" +
                "name='" + name + '\'' +
                ", unit='" + unit + '\'' +
                '}';
    }
}