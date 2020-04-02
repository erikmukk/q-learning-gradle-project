package qLearning;

import logger.Logger;
import math.Helpers;
import math.Normalization;
import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.Thermometer;
import org.concord.energy2d.model.Thermostat;

import java.io.Serializable;
import java.util.Arrays;
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

    private float episodeReward = 0;

    private static final float EPS_DECAY = 0.99f;
    private static final float LEARNING_RATE = 0.1f;
    private static final float DISCOUNT = 0.95f;
    private final Map<Integer, Float> allEpisodeRewards = new HashMap<>();
    private double epsilon = 0.9;
    private int correct = 0;
    private int incorrect = 0;
    private int prevCorrect = 0;
    private int prevIncorrect = 0;
    public int loops = 0;
    public int iterationLoops = 0;
    // New thing
    public String previousQKey;
    public int wantedAction;
    public int calculatedAction;
    public String observationSpace;
    public String newObservationSpace;

    Normalization tempNormalization;
    Normalization electricityPriceNormalization;

    public QTable(float minInsideTemp, float maxInsideTemp, float minOutsideTemp, float maxOutsideTemp, int actionsLength, float maxElectricityPrice) {
        initQTable(minInsideTemp, maxInsideTemp, minOutsideTemp, maxOutsideTemp, actionsLength);
        this.tempNormalization = new Normalization(maxOutsideTemp, 0, 0f, 1f);
        this.electricityPriceNormalization = new Normalization(maxElectricityPrice, 0f, 0f, 1f);
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
    }

    public String makeQTableKey(float insideTemp, float outsideTemp) {
        return insideTemp + "_" + outsideTemp;
    }

    public String calculateQTableKey (float insideTemp, float outsideTemp) {
        if (insideTemp > maxInsideTemp) return aboveMaxInsideTempKey;
        if (insideTemp < minInsideTemp) return belowMinInsideTempKey;
        if (outsideTemp > maxOutsideTemp) return aboveMaxOutsideTempKey;
        if (outsideTemp < minOutsideTemp) return belowMinOutsideTempKey;
        return makeQTableKey(insideTemp, outsideTemp);
    }

    public void startNewIteration(Logger logger, Environment environment) {
        this.epsilon = this.epsilon * EPS_DECAY;
        this.loops += 1;
        this.allEpisodeRewards.put(this.loops, this.episodeReward);
        logger.addToTotalTimeHeatingPerLoop(this.loops, environment.getTotalTimeHeating());
        logger.addToElectricityUsedPerLoopPerHr(this.loops, environment.getHeatingTimeAndPriceMap());
        logger.addToTemperatureAveragesPerLoopPerHr(this.loops, environment.getHeatingPeriodAndAvgTempMap());
        logger.addLoggedQTable(this);
        this.iterationLoops = 0;
        this.episodeReward = 0;
        System.out.println("correct: " + this.correct + "\tincorrect: " + this.incorrect + "\tloops: " + this.loops
                + "\tprevCorrect: " + this.prevCorrect+ "\tprevIncorrect: " + this.prevIncorrect);
        this.prevCorrect = 0;
        this.prevIncorrect = 0;
    }

    public void endTestIteration(Logger logger, Environment environment) {
        logger.addToTotalTimeHeatingPerLoop(this.loops, environment.getTotalTimeHeating());
        logger.addToElectricityUsedPerLoopPerHr(this.loops, environment.getHeatingTimeAndPriceMap());
        logger.addToTemperatureAveragesPerLoopPerHr(this.loops, environment.getHeatingPeriodAndAvgTempMap());
        logger.addLoggedQTable(this);
    }

    private double electricityPriceReward(float time, int action, Environment env) {
        int timeHr = (int) (time / 3600);
        if (timeHr > 23) {
            timeHr = 23;
        }
        HashMap<Integer, Float> prices = env.getElectricityStockPrice();
        float electricityPrice = prices.get(timeHr);
        if (action == env.HEAT) {
            return electricityPrice;
        }
        return 0;
    }

    public void doStepBeforeRunningOneMinute(Environment environment, Model2D model2D) {
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
        this.observationSpace = calculateQTableKey(insideTemp, outsideTemp);
        // Get actions
        float[] _actions = this.qTable.get(this.observationSpace);
        if (_actions == null) {
            _actions = this.qTable.get(aboveMaxInsideTempKey);
        }
        // Find action according to qTable or randomly
        if (Math.random() > epsilon) {
            this.calculatedAction = findArgmax(_actions);
        } else {
            this.calculatedAction = (int)(Math.random() * (environment.getActionSpace().length));
        }
        if (this.calculatedAction == environment.HEAT) {
            insideThermostat.getPowerSource().setPowerSwitch(true);
        } else if (this.calculatedAction == environment.STOP_HEATING) {
            insideThermostat.getPowerSource().setPowerSwitch(false);
        }
        // Take action
        this.wantedAction = environment.getCorrectAction();
        environment.takeAction(this.calculatedAction, model2D.getTime());
    }

    public void doWhenXTimeHasPassedForOneIterationTest(Environment environment, Model2D model2D) {
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
        String qTableKey = calculateQTableKey(insideTemp, outsideTemp);

        // Get actions
        float[] _actions = this.qTable.get(qTableKey);
        if (_actions == null) {
            _actions = this.qTable.get(aboveMaxInsideTempKey);
        }
        // Find action according to qTable
        int calculatedAction = findArgmax(_actions);
        if (calculatedAction == environment.HEAT) {
            insideThermostat.getPowerSource().setPowerSwitch(true);
        } else if (calculatedAction == environment.STOP_HEATING) {
            insideThermostat.getPowerSource().setPowerSwitch(false);
        }
        // Take action
        environment.takeAction(calculatedAction, model2D.getTime());
    }

    public void doWhenXTimeHasPassed(Environment environment, Model2D model2D) {
        float reward = 0;

        Thermometer insideThermometer = model2D.getThermometer("inside");
        Thermometer outsideThermometer = model2D.getThermometer("outside");
        Thermostat insideThermostat = model2D.getThermostats().get(0);
        float targetTemp = environment.targetTemp;
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

        // Calculate episode rewards
        if (insideTemp > this.maxInsideTemp) {
            insideTemp = this.maxInsideTemp;
        }
        // TODO: Here I changed to normalization [0, 1]
        reward += this.tempNormalization.normalize(Math.abs(targetTemp - insideTemp));
        // TODO: Here I changed to normalization [0, 1] * 0.2
        reward += this.electricityPriceNormalization.normalize(electricityPriceReward(model2D.getTime(), calculatedAction, environment)) * 0.2;
        // Make changes in environment
        environment.setInsideTemp(insideTemp);
        environment.setOutsideTemp(outsideTemp);

        this.newObservationSpace = calculateQTableKey(insideTemp, outsideTemp);
        float maxFutureQVal = findMax(this.qTable.get(this.newObservationSpace));
        float[] currentQValArray = this.qTable.get(this.observationSpace);
        float currentQVal = currentQValArray[this.calculatedAction];

        float newQVal = (1 - LEARNING_RATE) * currentQVal + LEARNING_RATE * (reward + DISCOUNT * maxFutureQVal);
        currentQValArray[this.calculatedAction] = newQVal;
        this.qTable.put(this.observationSpace, currentQValArray);
        this.observationSpace = this.newObservationSpace;

        if (this.wantedAction == this.calculatedAction) {
            this.prevCorrect += 1;
            this.correct += 1;
        } else {
            this.prevIncorrect += 1;
            this.incorrect += 1;
        }
        this.episodeReward += reward;
        this.iterationLoops += 1;
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

    public int getIterationLoops() {
        return iterationLoops;
    }
}
