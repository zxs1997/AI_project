package com.example.dante.ai_demo.ListViewPack;

public class RubbishInfo {
    private int ID;
    private String name;
    private String catalogue;

    public RubbishInfo(int ID, String name, String catalogue) {
        this.ID = ID;
        this.name = name;
        this.catalogue = catalogue;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getCatalogue() {
        return catalogue;
    }
}
