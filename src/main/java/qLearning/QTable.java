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
import java.util.Random;

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

    //private float EPS_DECAY = 0.99f;
    //private float LEARNING_RATE = 0.1f;
    //private float DISCOUNT = 0.95f;
    private float EPS_DECAY;
    private float LEARNING_RATE ;
    private float DISCOUNT;
    private final Map<Integer, Float> allEpisodeRewards = new HashMap<>();
    //private float epsilon = 0.9f;
    private float epsilon;
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
    public float temperatureRewardWeight;
    public float electricityRewardWeight;
    public Random randomGen;

    Normalization tempNormalization;
    Normalization electricityPriceNormalization;

    public QTable(float minInsideTemp, float maxInsideTemp, float minOutsideTemp, float maxOutsideTemp, int actionsLength, float maxElectricityValue, float epsilon, float epsilonDecay, float learningRate, float discount, float temperatureRewardWeight, float electricityRewardWeight, int minElectricityPrice, int maxElectricityPrice) {
        initQTable(minInsideTemp, maxInsideTemp, minOutsideTemp, maxOutsideTemp, actionsLength, minElectricityPrice, maxElectricityPrice);
        this.randomGen = new Random(10);
        this.epsilon = epsilon;
        this.EPS_DECAY = epsilonDecay;
        this.LEARNING_RATE = learningRate;
        this.DISCOUNT = discount;
        this.temperatureRewardWeight = temperatureRewardWeight;
        this.electricityRewardWeight = electricityRewardWeight;
        this.tempNormalization = new Normalization(maxOutsideTemp, 0, -1f, 0);
        this.electricityPriceNormalization = new Normalization(maxElectricityValue, 0f, -1f, 0);
    }

    private void initQTable(float minInsideTemp, float maxInsideTemp, float minOutsideTemp, float maxOutsideTemp, int actionsLength, int minElectricityPrice, int maxElectricityPrice) {
        this.minInsideTemp = minInsideTemp;
        this.maxInsideTemp = maxInsideTemp;
        this.minOutsideTemp = minOutsideTemp;
        this.maxOutsideTemp = maxOutsideTemp;
        this.qTable = new HashMap<>();
        for (int i=(int)minInsideTemp; i <= maxInsideTemp ; i+=1) {
            for (int j=(int)minOutsideTemp; j <= maxOutsideTemp + 0.1f ; j+=1) {
                for (int h = minElectricityPrice; h <= maxElectricityPrice ; h+=1) {
                    //String key = this.makeQTableKey(Helpers.roundFloat(i, 1), Helpers.roundFloat(j, 1));
                    for (int heatingStatus = 0 ; heatingStatus < 2 ; heatingStatus += 1) {
                        String key = this.makeQTableKey(i, j, h, heatingStatus);
                        this.qTable.put(key, new float[2]);
                    }
                }
            }
        }
        String[] specialKeys = new String[4];
        specialKeys[0] = this.belowMinInsideTempKey;
        specialKeys[1] = this.belowMinOutsideTempKey;
        specialKeys[2] = this.aboveMaxInsideTempKey;
        specialKeys[3] = this.aboveMaxOutsideTempKey;
        for (String key : specialKeys) {
            this.qTable.put(key, new float[2]);
        }
    }

    public String makeQTableKey(float insideTemp, float outsideTemp, float electricityPrice, int heatingStatus) {
        return insideTemp + "_" + outsideTemp + "_" + electricityPrice + "_" + heatingStatus;
    }

    public String makeQTableKey(int insideTemp, int outsideTemp, int electricityPrice, int heatingStatus) {
        return insideTemp + "_" + outsideTemp + "_" + electricityPrice + "_" + heatingStatus;
    }

    public String calculateQTableKey (float insideTemp, float outsideTemp, float electricityPrice, int heatingStatus) {
        if (insideTemp > maxInsideTemp) return aboveMaxInsideTempKey;
        if (insideTemp < minInsideTemp) return belowMinInsideTempKey;
        if (outsideTemp > maxOutsideTemp) return aboveMaxOutsideTempKey;
        if (outsideTemp < minOutsideTemp) return belowMinOutsideTempKey;
        return makeQTableKey(insideTemp, outsideTemp, electricityPrice, heatingStatus);
    }

    public String calculateQTableKey (int insideTemp, int outsideTemp, int electricityPrice, int heatingStatus) {
        if (insideTemp > maxInsideTemp) return aboveMaxInsideTempKey;
        if (insideTemp < minInsideTemp) return belowMinInsideTempKey;
        if (outsideTemp > maxOutsideTemp) return aboveMaxOutsideTempKey;
        if (outsideTemp < minOutsideTemp) return belowMinOutsideTempKey;
        return makeQTableKey(insideTemp, outsideTemp, electricityPrice, heatingStatus);
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

    public void doStepBeforeRunningXMinutes(Environment environment, Model2D model2D) {
        Thermometer insideThermometer = model2D.getThermometer("inside");
        Thermometer outsideThermometer = model2D.getThermometer("outside");
        Thermostat insideThermostat = model2D.getThermostats().get(0);
        int bgTemp = Math.round(model2D.getBackgroundTemperature());
        int insideTempKey;
        int outsideTempKey;
        try {
            insideTempKey = Math.round(insideThermometer.getCurrentData());
        } catch (Exception e) {
            insideTempKey = bgTemp;
        }
        try {
            outsideTempKey = Math.round(outsideThermometer.getCurrentData());
        } catch (Exception e) {
            outsideTempKey = bgTemp;
        }
        int electricityPrice = environment.getElectricityPriceAt(model2D.getTime());
        int heatingStatus = insideThermostat.getPowerSource().getPowerSwitch() ? 0 : 1;
        this.observationSpace = calculateQTableKey(insideTempKey, outsideTempKey, electricityPrice, heatingStatus);
        // Get actions
        float[] _actions = this.qTable.get(this.observationSpace);
        if (_actions == null) {
            _actions = this.qTable.get(aboveMaxInsideTempKey);
        }
        // Find action according to qTable or randomly
        if (this.randomGen.nextFloat() > epsilon) {
            this.calculatedAction = findArgmax(_actions);
        } else {
            this.calculatedAction = (int)(this.randomGen.nextFloat() * (environment.getActionSpace().length));
        }
        // Set model2D heat source to on/off
        if (this.calculatedAction == environment.HEAT) {
            insideThermostat.getPowerSource().setPowerSwitch(true);
        } else if (this.calculatedAction == environment.STOP_HEATING) {
            insideThermostat.getPowerSource().setPowerSwitch(false);
        }
        // Take action and set wantedAction for logging purposes
        this.wantedAction = environment.getCorrectAction();
        environment.takeAction(this.calculatedAction, model2D.getTime());
    }

    public void doWhenXTimeHasPassed(Environment environment, Model2D model2D) {
        float reward = 0;
        Thermometer insideThermometer = model2D.getThermometer("inside");
        Thermometer outsideThermometer = model2D.getThermometer("outside");
        Thermostat insideThermostat = model2D.getThermostats().get(0);
        int targetTemp = Math.round(environment.targetTemp);
        int bgTemp = Math.round(model2D.getBackgroundTemperature());
        float insideTemp;
        int insideTempKey;
        float outsideTemp;
        int outsideTempKey;
        try {
            insideTemp = insideThermometer.getCurrentData();
            insideTempKey = Math.round(insideThermometer.getCurrentData());
        } catch (Exception e) {
            insideTemp = bgTemp;
            insideTempKey = bgTemp;
        }
        try {
            outsideTemp = outsideThermometer.getCurrentData();
            outsideTempKey = Math.round(outsideThermometer.getCurrentData());
        } catch (Exception e) {
            outsideTemp = bgTemp;
            outsideTempKey = bgTemp;
        }

        // Calculate episode reward
        float tempReward = this.tempNormalization.normalize(Math.abs(targetTemp - insideTemp)) * this.temperatureRewardWeight;
        reward += tempReward;
        double electricityReward = this.electricityPriceNormalization.normalize(electricityPriceReward(model2D.getTime(), calculatedAction, environment)) * this.electricityRewardWeight;
        reward += electricityReward;

        // Make changes in environment
        environment.setInsideTemp(insideTemp);
        environment.setOutsideTemp(outsideTemp);

        int electricityPrice = environment.getElectricityPriceAt(model2D.getTime());
        int heatingStatus = insideThermostat.getPowerSource().getPowerSwitch() ? 0 : 1;
        this.newObservationSpace = calculateQTableKey(insideTempKey, outsideTempKey, electricityPrice, heatingStatus);
        float maxFutureQVal = findMax(this.qTable.get(this.newObservationSpace));
        float[] currentQValArray = this.qTable.get(this.observationSpace);
        float currentQVal = currentQValArray[this.calculatedAction];

        float newQVal = currentQVal + LEARNING_RATE * (reward + DISCOUNT * maxFutureQVal - currentQVal);
        currentQValArray[this.calculatedAction] = newQVal;
        this.qTable.put(this.observationSpace, currentQValArray);
        this.observationSpace = this.newObservationSpace;

        // For logging. Has nothing to do with qlearning itself
        if (this.wantedAction == this.calculatedAction) {
            this.prevCorrect += 1;
            this.correct += 1;
        } else {
            this.prevIncorrect += 1;
            this.incorrect += 1;
        }
        this.episodeReward += reward;
        this.iterationLoops += 1;
        ////////////////////////////////////////////////////////
    }

    private static int findArgmax(float[] array) {
        float max = -2000000000f;
        int index = 0;
        for (int i = 0 ; i < array.length ; i++) {
            if (array[i] > max) {
                max = array[i];
                index = i;
            }
        } return index;
    }

    private static float findMax(float[] array) {
        float max = -2000000000f;
        for (float i : array) {
            if (i > max) {
                max = i;
            }
        } return max;
    }

    public HashMap<String, float[]> getqTable() {
        return qTable;
    }

    public int getLoops() {
        return loops;
    }

    public Map<Integer, Float> getAllEpisodeRewards() {
        return allEpisodeRewards;
    }

}
