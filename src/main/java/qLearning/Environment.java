package qLearning;

import math.Helpers;

public class Environment {
    boolean humanPresence;
    float insideTemp;
    float outsideTemp;
    float targetTemp;
    boolean isHeating;
    public final int DO_NOTHING = 0;
    public final int HEAT = 1;
    public final int STOP_HEATING = 2;
    private final int[] actionSpace = new int[]{HEAT, STOP_HEATING, DO_NOTHING};

    public Environment(float outsideTemp, float insideTemp, float targetTemp) {
        this.humanPresence = Helpers.getRandomBoolean();
        this.isHeating = Helpers.getRandomBoolean();
        this.insideTemp = insideTemp;
        this.outsideTemp = outsideTemp;
        this.targetTemp = targetTemp;
    }

    public void takeAction(int choice) {
        if (choice == HEAT) {
            this.isHeating = true;
        } else if (choice == STOP_HEATING) {
            this.isHeating = false;
        }
    }

    public int[] getActionSpace() {
        return actionSpace;
    }

    public int getCorrectAction() {
        if (this.isHeating) {
            if (this.outsideTemp > this.insideTemp) {
                if (this.outsideTemp > this.targetTemp) {
                    return STOP_HEATING;
                }
            } else {
                if (this.insideTemp >= this.targetTemp) {
                    return STOP_HEATING;
                }
            }
            return DO_NOTHING;
        } else {
            if (this.outsideTemp > this.insideTemp) {
                if (this.outsideTemp >= this.targetTemp) {
                    return DO_NOTHING;
                } else if (this.insideTemp >= this.targetTemp) {
                    return DO_NOTHING;
                }
            } else {
                if (this.insideTemp >= this.targetTemp) {
                    return DO_NOTHING;
                }
            }
            return HEAT;
        }
    }

    public float getInsideTemp() {
        return insideTemp;
    }

    public void setInsideTemp(float insideTemp) {
        this.insideTemp = insideTemp;
    }

    public float getOutsideTemp() {
        return outsideTemp;
    }

    public void setOutsideTemp(float outsideTemp) {
        this.outsideTemp = outsideTemp;
    }

    @Override
    public String toString() {
        return String.format("Is heating: %b." +
                        " Human present: %b." +
                        " Inside temp: %f. " +
                        " Outside temp: %f.",
                this.isHeating,
                this.humanPresence,
                this.insideTemp,
                this.outsideTemp);
    }
}
