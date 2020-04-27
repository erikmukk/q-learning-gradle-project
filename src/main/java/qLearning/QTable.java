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


    private final Map<Integer, Float> allEpisodeRewards = new HashMap<>();
    public int loops = 0;
    // New thing
    public int calculatedAction;
    public String observationSpace;
    public Random randomGen;

    Normalization tempNormalization;
    Normalization electricityPriceNormalization;

    public QTable(float minInsideTemp, float maxInsideTemp, float minOutsideTemp, float maxOutsideTemp, int actionsLength, float maxElectricityPrice) {
        this.maxInsideTemp = maxInsideTemp;
        this.maxOutsideTemp = maxOutsideTemp;
        this.minInsideTemp = minInsideTemp;
        this.minOutsideTemp = minOutsideTemp;
        this.randomGen = new Random(10);

        this.tempNormalization = new Normalization(maxOutsideTemp, 0, 0f, 1f);
        this.electricityPriceNormalization = new Normalization(maxElectricityPrice, 0f, 0f, 1f);
    }

    public String makeQTableKey(float insideTemp, float outsideTemp, float electricityPrice) {
        return insideTemp + "_" + outsideTemp + "_" + electricityPrice;
    }

    public String makeQTableKey(int insideTemp, int outsideTemp, int electricityPrice) {
        return insideTemp + "_" + outsideTemp + "_" + electricityPrice;
    }

    public String calculateQTableKey (float insideTemp, float outsideTemp, float electricityPrice) {
        if (insideTemp > maxInsideTemp) return aboveMaxInsideTempKey;
        if (insideTemp < minInsideTemp) return belowMinInsideTempKey;
        if (outsideTemp > maxOutsideTemp) return aboveMaxOutsideTempKey;
        if (outsideTemp < minOutsideTemp) return belowMinOutsideTempKey;
        return makeQTableKey(insideTemp, outsideTemp, electricityPrice);
    }

    public String calculateQTableKey (int insideTemp, int outsideTemp, int electricityPrice) {
        if (insideTemp > maxInsideTemp) return aboveMaxInsideTempKey;
        if (insideTemp < minInsideTemp) return belowMinInsideTempKey;
        if (outsideTemp > maxOutsideTemp) return aboveMaxOutsideTempKey;
        if (outsideTemp < minOutsideTemp) return belowMinOutsideTempKey;
        return makeQTableKey(insideTemp, outsideTemp, electricityPrice);
    }

    public void startNewIteration(Logger logger, Environment environment) {
        this.loops += 1;
        logger.addToTotalTimeHeatingPerLoop(this.loops, environment.getTotalTimeHeating());
        logger.addToElectricityUsedPerLoopPerHr(this.loops, environment.getHeatingTimeAndPriceMap());
        logger.addToTemperatureAveragesPerLoopPerHr(this.loops, environment.getHeatingPeriodAndAvgTempMap());
    }

    private double electricityPriceReward(float time, int action, Environment env) {
        int timeHr;
        if (time > 86400) {
            timeHr = (int) ((time - 86400) / 3600);
        } else {
            timeHr = (int) (time / 3600);
        }

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
        int insideTemp;
        int outsideTemp;
        try {
            //insideTemp = Helpers.roundFloat(insideThermometer.getCurrentData(), 1);
            insideTemp = Math.round(insideThermometer.getCurrentData());
        } catch (Exception e) {
            insideTemp = bgTemp;
        }
        try {
            //outsideTemp = Helpers.roundFloat(outsideThermometer.getCurrentData(), 1);
            outsideTemp = Math.round(outsideThermometer.getCurrentData());
        } catch (Exception e) {
            outsideTemp = bgTemp;
        }
        int electricityPrice = environment.getElectricityPriceAt(model2D.getTime());
        this.observationSpace = calculateQTableKey(insideTemp, outsideTemp, electricityPrice);
        // Get actions
        float[] _actions = this.qTable.get(this.observationSpace);
        if (_actions == null) {
            _actions = this.qTable.get(aboveMaxInsideTempKey);
        }
        // Find action according to qTable or randomly
        this.calculatedAction = findArgmax(_actions);
        environment.setInsideTemp(insideTemp);
        environment.setOutsideTemp(outsideTemp);

        // Set model2D heat source to on/off
        if (this.calculatedAction == environment.HEAT) {
            insideThermostat.getPowerSource().setPowerSwitch(true);
        } else if (this.calculatedAction == environment.STOP_HEATING) {
            insideThermostat.getPowerSource().setPowerSwitch(false);
        }
        environment.takeAction(this.calculatedAction, model2D.getTime());
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

    public HashMap<String, float[]> getqTable() {
        return qTable;
    }

    public Map<Integer, Float> getAllEpisodeRewards() {
        return allEpisodeRewards;
    }

    public void setqTable(HashMap<String, float[]> qTable) {
        this.qTable = qTable;
    }
}
