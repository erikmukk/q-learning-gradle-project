package logger;

import qLearning.QTable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class Logger implements Serializable {

    // Map<Episode#, TimeHeating>
    HashMap<Integer, Integer> totalTimeHeatingPerLoop;
    HashMap<Integer, HashMap<Integer, Integer>> electricityUsedPerLoopPerHr;
    HashMap<Integer, HashMap<Integer, List<Float>>> temperatureAveragesPerLoopPerHr;

    public Logger() {
        this.totalTimeHeatingPerLoop = new HashMap<>();
        this.electricityUsedPerLoopPerHr = new HashMap<>();
        this.temperatureAveragesPerLoopPerHr = new HashMap<>();
    }

    public void addToTotalTimeHeatingPerLoop(int loopNr, int time) {
        this.totalTimeHeatingPerLoop.put(loopNr, time);
    }

    public void addToElectricityUsedPerLoopPerHr(int loopNr, HashMap<Integer, Integer> map) {
        this.electricityUsedPerLoopPerHr.put(loopNr, map);
    }

    public void addToTemperatureAveragesPerLoopPerHr(int loopNr, HashMap<Integer, List<Float>> map) {
        this.temperatureAveragesPerLoopPerHr.put(loopNr, map);
    }

    public HashMap<Integer, Integer> getTotalTimeHeatingPerLoop() {
        return totalTimeHeatingPerLoop;
    }

    public HashMap<Integer, HashMap<Integer, Integer>> getElectricityUsedPerLoopPerHr() {
        return electricityUsedPerLoopPerHr;
    }

    public HashMap<Integer, HashMap<Integer, List<Float>>> getTemperatureAveragesPerLoopPerHr() {
        return temperatureAveragesPerLoopPerHr;
    }
}
