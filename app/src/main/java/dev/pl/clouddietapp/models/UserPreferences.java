package dev.pl.clouddietapp.models;

public class UserPreferences {
    private int maxSupermarketDistance;
    private boolean isVegetarian;

    public UserPreferences(int maxSupermarketDistance, boolean isVegetarian) {
        this.maxSupermarketDistance = maxSupermarketDistance;
        this.isVegetarian = isVegetarian;
    }

    public UserPreferences() {}

    public int getMaxSupermarketDistance() {
        return maxSupermarketDistance;
    }

    public void setMaxSupermarketDistance(int maxSupermarketDistance) {
        this.maxSupermarketDistance = maxSupermarketDistance;
    }

    public boolean isVegetarian() {
        return isVegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        isVegetarian = vegetarian;
    }

    @Override
    public String toString() {
        return "UserPreferences{" +
                "maxSupermarketDistance=" + maxSupermarketDistance +
                ", isVegetarian=" + isVegetarian +
                '}';
    }
}
