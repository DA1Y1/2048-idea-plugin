package com.daie.game;

/**
 * @author daiyi
 * @date 2021/11/18
 */
public class Tile {
    private boolean merged;
    private int value;

    Tile(int val) {
        value = val;
    }

    boolean canMergeWith(Tile other) {
        return !merged && other != null && !other.merged && value == other.getValue();
    }

    int mergeWith(Tile other) {
        if (canMergeWith(other)) {
            value *= 2;
            merged = true;
            return value;
        }
        return -1;
    }

    int getValue() {
        return value;
    }

    void setMerged(boolean m) {
        merged = m;
    }
}
