package qLearning;

import math.Helpers;
import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.Thermometer;
import org.concord.energy2d.model.Thermostat;

import java.util.HashMap;
import java.util.Map;

public class QTable {

    HashMap<String, float[]> qTable;
    public String belowMinInsideTempKey = "-_";
    public String aboveMaxInsideTempKey = "+_";
    public String belowMinOutsideTempKey = "_-";
    public String aboveMaxOutsideTempKey = "_+";
    private float minInsideTemp;
    private float maxInsideTemp;
    private float minOutsideTemp;
    private float maxOutsideTemp;


    static float episodeReward = 0;

    private static final int CORRECT_HEATING_REWARD = 100;
    private static final int INCORRECT_HEATING_PENALTY = -400;
    private static final float EPS_DECAY = 0.9998f;
    private static final float LEARNING_RATE = 0.1f;
    private static final float DISCOUNT = 0.95f;
    static double epsilon = 0.9;
    static int correct = 0;
    static int incorrect = 0;
    static int prevCorrect = 0;
    static int prevIncorrect = 0;
    static int loops = 0;

    public QTable(float minInsideTemp, float maxInsideTemp, float minOutsideTemp, float maxOutsideTemp, int actionsLength) {
        initQTable(minInsideTemp, maxInsideTemp, minOutsideTemp, maxOutsideTemp, actionsLength);
    }

    private void initQTable(float minInsideTemp, float maxInsideTemp, float minOutsideTemp, float maxOutsideTemp, int actionsLength) {
        this.minInsideTemp = minInsideTemp;
        this.maxInsideTemp = maxInsideTemp;
        this.minOutsideTemp = minOutsideTemp;
        this.maxOutsideTemp = maxOutsideTemp;
        qTable = new HashMap<>();
        for (float i=minInsideTemp; i <= maxInsideTemp ; i+=0.1f) {
            for (float j=minOutsideTemp; j <= maxOutsideTemp + 0.1f ; j+=0.1f) {
                String key = this.makeQTableKey(Helpers.roundFloat(i, 1), Helpers.roundFloat(j, 1));
                float[] arr = new float[actionsLength];
                for (int k = 0 ; k < actionsLength ; k++) {
                    arr[k] = 0f;
                }
                qTable.put(key, arr);
            }
        }
        qTable.put(belowMinInsideTempKey, new float[]{0f, 0f, 0f});
        qTable.put(belowMinOutsideTempKey, new float[]{0f, 0f, 0f});
        qTable.put(aboveMaxInsideTempKey, new float[]{0f, 0f, 0f});
        qTable.put(aboveMaxOutsideTempKey, new float[]{0f, 0f, 0f});
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
        epsilon = epsilon * EPS_DECAY;
        loops += 1;
        System.out.println("correct: " + correct + "\tincorrect: " + incorrect + "\tloops: " + loops
                + "\tprevCorrect: " + prevCorrect+ "\tprevIncorrect: " + prevIncorrect);
        prevCorrect = 0;
        prevIncorrect = 0;
    }

    public void calculateQTableValue(Environment environment, Model2D model2D) {
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
        int wantedAction = environment.getCorrectAction(20);

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
            prevCorrect += 1;
            correct += 1;
        } else {
            reward += INCORRECT_HEATING_PENALTY;
            prevIncorrect += 1;
            incorrect += 1;
        }

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
        qTable.put(qTableKey2, tempValues);
        episodeReward += reward;

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
}
