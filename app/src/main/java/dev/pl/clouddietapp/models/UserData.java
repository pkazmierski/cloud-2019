package dev.pl.clouddietapp.models;

import java.util.Objects;

public class UserData {
    private String username;
    private String fullName;
    private int age;
    private int heightInCm;
    private int weight;
    private int physicalActivity;
    private Gender gender;
    private String location;

    public UserData(String username, String fullName, int age, int heightInCm, int weight, int physicalActivity, Gender gender, String location) {
        this.username = username;
        this.fullName = fullName;
        this.age = age;
        this.heightInCm = heightInCm;
        this.weight = weight;
        this.physicalActivity = physicalActivity;
        this.gender = gender;
        this.location = location;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getHeightInCm() {
        return heightInCm;
    }

    public void setHeightInCm(int heightInCm) {
        this.heightInCm = heightInCm;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getPhysicalActivity() {
        return physicalActivity;
    }

    public void setPhysicalActivity(int physicalActivity) {
        this.physicalActivity = physicalActivity;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserData userData = (UserData) o;
        return username.equals(userData.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return "UserData{" +
                "username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", age=" + age +
                ", heightInCm=" + heightInCm +
                ", weight=" + weight +
                ", physicalActivity=" + physicalActivity +
                ", gender=" + gender +
                ", location='" + location + '\'' +
                '}';
    }
}
