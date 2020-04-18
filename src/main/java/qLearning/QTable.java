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
    public int loops = 0;

    public QTable() {
    }

    public void startNewIteration(Logger logger, Environment environment) {
        this.loops += 1;
        logger.addToTotalTimeHeatingPerLoop(this.loops, environment.getTotalTimeHeating());
        logger.addToElectricityUsedPerLoopPerHr(this.loops, environment.getHeatingTimeAndPriceMap());
        logger.addToTemperatureAveragesPerLoopPerHr(this.loops, environment.getHeatingPeriodAndAvgTempMap());
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
        environment.setInsideTemp(insideTemp);
        environment.setOutsideTemp(outsideTemp);

        // Set model2D heat source to on/off
        if (insideThermostat.getPowerSource().getPowerSwitch()) {
            environment.takeAction(environment.HEAT, model2D.getTime());
        } else {
            environment.takeAction(environment.STOP_HEATING, model2D.getTime());
        }
        System.out.println(insideTemp);
    }

    public void setqTable(HashMap<String, float[]> qTable) {
        this.qTable = qTable;
    }
}
