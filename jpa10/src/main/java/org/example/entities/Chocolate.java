package org.example.entities;

import javax.persistence.Entity;

@Entity
public class Chocolate extends Product {

    private int kcal;

    public int getKcal() {
        return kcal;
    }

    public void setKcal(int kcal) {
        this.kcal = kcal;
    }
}

