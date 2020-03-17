package qLearning;

import math.Helpers;

import java.io.Serializable;
import java.util.HashMap;

public class Environment implements Serializable {
    boolean humanPresence;
    float insideTemp;
    float outsideTemp;
    float targetTemp;
    boolean isHeating;
    int totalTimeHeating = 0;
    HashMap<Integer, Float> electricityStockPrice;
    float averageStockPrice;

    //HashMap<Iteration, HashMap<ElectricityPeriodHour, HeatedTimeInMins>>
    HashMap<Integer, HashMap<Integer, Integer>> heatingTimeAndPriceMap;
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
        this.heatingTimeAndPriceMap = new HashMap<>();
        initElectricityPrice();
    }

    private void initElectricityPrice() {
        this.electricityStockPrice = new HashMap<>();
        this.electricityStockPrice.put(0, 17.59f);
        this.electricityStockPrice.put(1, 14.96f);
        this.electricityStockPrice.put(2, 14.91f);
        this.electricityStockPrice.put(3, 14.93f);
        this.electricityStockPrice.put(4, 21.68f);
        this.electricityStockPrice.put(5, 16.09f);
        this.electricityStockPrice.put(6, 20.02f);
        this.electricityStockPrice.put(7, 34.45f);
        this.electricityStockPrice.put(8, 41.07f);
        this.electricityStockPrice.put(9, 38.58f);
        this.electricityStockPrice.put(10, 25.85f);
        this.electricityStockPrice.put(11, 30.70f);
        this.electricityStockPrice.put(12, 39.31f);
        this.electricityStockPrice.put(13, 39.72f);
        this.electricityStockPrice.put(14, 33.52f);
        this.electricityStockPrice.put(15, 22.79f);
        this.electricityStockPrice.put(16, 30.03f);
        this.electricityStockPrice.put(17, 50.89f);
        this.electricityStockPrice.put(18, 62.43f);
        this.electricityStockPrice.put(19, 35.07f);
        this.electricityStockPrice.put(20, 18.61f);
        this.electricityStockPrice.put(21, 17.20f);
        this.electricityStockPrice.put(22, 12.09f);
        this.electricityStockPrice.put(23, 9.38f);
        this.averageStockPrice = 27.58f;
    }

    private void addTimeAndPrice(float time, int loopNr) {
        int timeHr = (int) (time / 3600);
        if (timeHr > 23) {
            timeHr = 23;
        }
        // Increment heating duration in loop at x-time
        if (this.heatingTimeAndPriceMap.containsKey(loopNr)) {
            HashMap<Integer, Integer> entry = this.heatingTimeAndPriceMap.get(loopNr);
            if (entry.containsKey(timeHr)) {
                entry.put(timeHr, entry.get(timeHr) + 30);
            } else {
                entry.put(timeHr, 30);
            } this.heatingTimeAndPriceMap.put(loopNr, entry);
        } else {
            HashMap<Integer, Integer> newEntry = new HashMap<>();
            newEntry.put(timeHr, 30);
            this.heatingTimeAndPriceMap.put(loopNr, newEntry);
        }
    }

    public void takeAction(int choice, float time, int loopNr) {
        if (choice == HEAT) {
            addTimeAndPrice(time, loopNr);
            this.isHeating = true;
            this.totalTimeHeating += 30;
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

    public HashMap<Integer, Float> getElectricityStockPrice() {
        return electricityStockPrice;
    }

    public float getAverageStockPrice() {
        return averageStockPrice;
    }

    public HashMap<Integer, HashMap<Integer, Integer>> getHeatingTimeAndPriceMap() {
        return heatingTimeAndPriceMap;
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
