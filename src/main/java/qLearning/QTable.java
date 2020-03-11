package qLearning;

import math.Helpers;
import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.Thermometer;
import org.concord.energy2d.model.Thermostat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class QTable implements Serializable {

    HashMap<String, float[]> qTable;
    public String belowMinInsideTempKey = "-_";
    public String aboveMaxInsideTempKey = "+_";
    public String belowMinOutsideTempKey = "_-";
    public String aboveMaxOutsideTempKey = "_+";
    private float minInsideTemp;
    private float maxInsideTemp;
    private float minOutsideTemp;
    private float maxOutsideTemp;

    private HashMap<Integer, Float> electricityStockPrice;
    private float averageStockPrice;

    private float episodeReward = 0;

    private static final int CORRECT_HEATING_REWARD = 100;
    private static final int GOOD_ELECTRICITY_PRICE_REWARD = 25;
    private static final int BAD_ELECTRICITY_PRICE_PENALTY = -50;
    private static final int INCORRECT_HEATING_PENALTY = -400;
    private static final float EPS_DECAY = 0.9998f;
    private static final float LEARNING_RATE = 0.1f;
    private static final float DISCOUNT = 0.95f;
    private final Map<Integer, Float> allEpisodeRewards = new HashMap<>();
    private double epsilon = 0.9;
    private int correct = 0;
    private int incorrect = 0;
    private int prevCorrect = 0;
    private int prevIncorrect = 0;
    public int loops = 0;

    public QTable(float minInsideTemp, float maxInsideTemp, float minOutsideTemp, float maxOutsideTemp, int actionsLength) {
        initQTable(minInsideTemp, maxInsideTemp, minOutsideTemp, maxOutsideTemp, actionsLength);
    }

    private void initQTable(float minInsideTemp, float maxInsideTemp, float minOutsideTemp, float maxOutsideTemp, int actionsLength) {
        this.minInsideTemp = minInsideTemp;
        this.maxInsideTemp = maxInsideTemp;
        this.minOutsideTemp = minOutsideTemp;
        this.maxOutsideTemp = maxOutsideTemp;
        this.qTable = new HashMap<>();
        for (float i=minInsideTemp; i <= maxInsideTemp ; i+=0.1f) {
            for (float j=minOutsideTemp; j <= maxOutsideTemp + 0.1f ; j+=0.1f) {
                String key = this.makeQTableKey(Helpers.roundFloat(i, 1), Helpers.roundFloat(j, 1));
                float[] arr = new float[actionsLength];
                for (int k = 0 ; k < actionsLength ; k++) {
                    arr[k] = 0f;
                }
                this.qTable.put(key, arr);
            }
        }
        this.qTable.put(belowMinInsideTempKey, new float[]{0f, 0f, 0f});
        this.qTable.put(belowMinOutsideTempKey, new float[]{0f, 0f, 0f});
        this.qTable.put(aboveMaxInsideTempKey, new float[]{0f, 0f, 0f});
        this.qTable.put(aboveMaxOutsideTempKey, new float[]{0f, 0f, 0f});
        this.initElectricity();
    }

    private void initElectricity() {
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

    public String makeQTableKey(float insideTemp, float outsideTemp) {
        return insideTemp + "_" + outsideTemp;
    }

    public String calculateQTableKey (float envInsideTemp, float envOutsideTemp, float insideTemp, float outsideTemp) {
        if (envInsideTemp > maxInsideTemp) {
            return aboveMaxInsideTempKey;
        } else if (envInsideTemp < minInsideTemp) {
            return belowMinInsideTempKey;
        } else if (envOutsideTemp > maxOutsideTemp) {
            return aboveMaxOutsideTempKey;
        } else if (envOutsideTemp < minOutsideTemp) {
            return belowMinOutsideTempKey;
        } else {
            return makeQTableKey(insideTemp, outsideTemp);
        }
    }

    public void startNewIteration() {
        this.epsilon = this.epsilon * EPS_DECAY;
        this.loops += 1;
        this.allEpisodeRewards.put(this.loops, this.episodeReward);
        this.episodeReward = 0;
        System.out.println("correct: " + this.correct + "\tincorrect: " + this.incorrect + "\tloops: " + this.loops
                + "\tprevCorrect: " + this.prevCorrect+ "\tprevIncorrect: " + this.prevIncorrect);
        this.prevCorrect = 0;
        this.prevIncorrect = 0;
    }

    private int electricityPriceReward(float time, int action, Environment env) {
        int timeHr = (int) (time / 36000);
        if (timeHr > 23) {
            timeHr = 23;
        }
        float electricityPrice = this.electricityStockPrice.get(timeHr);
        if (action == env.HEAT) {
            if (electricityPrice > this.averageStockPrice + 10) {
                return BAD_ELECTRICITY_PRICE_PENALTY;
            } else {
                return GOOD_ELECTRICITY_PRICE_REWARD;
            }
        }
        return 0;
    }

    public void doWhenXTimeHasPassed(Environment environment, Model2D model2D) {
        int reward = 0;
        int[] actionSpace = environment.getActionSpace();
        Thermometer insideThermometer = model2D.getThermometer("inside");
        Thermometer outsideThermometer = model2D.getThermometer("outside");
        Thermostat insideThermostat = model2D.getThermostats().get(0);
        float insideTemp;
        float outsideTemp;
        try {
            insideTemp = Helpers.roundFloat(insideThermometer.getCurrentData(), 1);
        } catch (Exception e) {
            insideTemp = 0.0f;
        }
        try {
            outsideTemp = Helpers.roundFloat(outsideThermometer.getCurrentData(), 1);
        } catch (Exception e) {
            outsideTemp = 0.0f;
        }

        float envInsideTemp = environment.getInsideTemp();
        float envOutsideTemp = environment.getOutsideTemp();
        String qTableKey = calculateQTableKey(envInsideTemp, envOutsideTemp, insideTemp, outsideTemp);

        // Get actions
        float[] _actions = this.qTable.get(qTableKey);
        int calculatedAction;
        if (Math.random() > epsilon) {
            calculatedAction = findArgmax(_actions);
        } else {
            calculatedAction = (int)(Math.random() * (actionSpace.length));
        }
        int wantedAction = environment.getCorrectAction();

        // Take action
        environment.takeAction(calculatedAction);
        if (calculatedAction == environment.HEAT) {
            insideThermostat.getPowerSource().setPowerSwitch(true);
        } else if (calculatedAction == environment.STOP_HEATING) {
            insideThermostat.getPowerSource().setPowerSwitch(false);
        }
        environment.setInsideTemp(insideTemp);
        environment.setOutsideTemp(outsideTemp);

        // Calculate episode rewards
        if (wantedAction == calculatedAction) {
            reward += CORRECT_HEATING_REWARD;
            this.prevCorrect += 1;
            this.correct += 1;
        } else {
            reward += INCORRECT_HEATING_PENALTY;
            this.prevIncorrect += 1;
            this.incorrect += 1;
        }

        reward += electricityPriceReward(model2D.getTime(), calculatedAction, environment);

        // Calculate qTable values
        float envInsideTemp2 = environment.getInsideTemp();
        float envOutsideTemp2 = environment.getOutsideTemp();
        String qTableKey2 = calculateQTableKey(envInsideTemp2, envOutsideTemp2, insideTemp, outsideTemp);
        float maxFutureQValue = findMax(qTable.get(qTableKey2));
        float currentQ = qTable.get(qTableKey2)[calculatedAction];
        float newQ;
        if (reward == CORRECT_HEATING_REWARD) {
            newQ = CORRECT_HEATING_REWARD;
        } else {
            newQ = (1 - LEARNING_RATE) * currentQ + LEARNING_RATE * (reward + DISCOUNT * maxFutureQValue);
        }
        // Set new qTable values
        float[] tempValues = qTable.get(qTableKey2);
        tempValues[calculatedAction] = newQ;
        this.qTable.put(qTableKey2, tempValues);
        this.episodeReward += reward;
    }

    private static int findArgmax(float[] array) {
        float max = -2000000000;
        int index = 0;
        for (int i = 0 ; i < array.length ; i++) {
            if (array[i] > max) {
                max = i;
                index = i;
            }
        } return index;
    }

    private static float findMax(float[] array) {
        float max = -2000000000;
        for (float i : array) {
            if (i > max) {
                max = i;
            }
        } return max;
    }

    public HashMap<String, float[]> getqTable() {
        return qTable;
    }

    public String getBelowMinInsideTempKey() {
        return belowMinInsideTempKey;
    }

    public String getAboveMaxInsideTempKey() {
        return aboveMaxInsideTempKey;
    }

    public String getBelowMinOutsideTempKey() {
        return belowMinOutsideTempKey;
    }

    public String getAboveMaxOutsideTempKey() {
        return aboveMaxOutsideTempKey;
    }

    public int getLoops() {
        return loops;
    }

    public Map<Integer, Float> getAllEpisodeRewards() {
        return allEpisodeRewards;
    }


}
