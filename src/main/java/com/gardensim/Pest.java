package com.gardensim;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a pest entity in the garden simulation.
 * Each pest has a position (row, col), a count, and optionally an associated plant type it attacks.
 */
public class Pest {
    /** Global list of all pests present in the garden. */
    public static final List<Pest> pests = new ArrayList<>();

    private int row;
    private int col;
    private int numPests;
    private String plantFood;

    public Pest(int row, int col, int numPests) {
        this.row = row;
        this.col = col;
        this.numPests = numPests;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getNumPests() {
        return numPests;
    }

    public void setNumPests(int numPests) {
        this.numPests = numPests;
    }

    public String getPlantFood() {
        return plantFood;
    }

    public void setPlantFood(String plantFood) {
        this.plantFood = plantFood;
    }

    @Override
    public String toString() {
        return "Pest{" +
                "row=" + row +
                ", col=" + col +
                ", numPests=" + numPests +
                ", plantFood='" + plantFood + '\'' +
                '}';
    }
}
