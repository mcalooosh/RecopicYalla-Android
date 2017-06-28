package com.yallaproductionz.recopicyalla.Adapter;

/**
 * Created by Aloush on 5/26/2017.
 */
public class Prediction {
    private String name;
    private float percentage;

    public Prediction(String name, float percentage){

        this.name=name;
        this.percentage=percentage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }
}
