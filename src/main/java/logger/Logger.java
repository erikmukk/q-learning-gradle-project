package logger;

import qLearning.QTable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class Logger implements Serializable {

    QTable loggedQTable;
    // Map<Episode#, TimeHeating>
    HashMap<Integer, Integer> totalTimeHeatingPerLoop;
    HashMap<Integer, HashMap<Integer, Integer>> electricityUsedPerLoopPerHr;
    HashMap<Integer, HashMap<Integer, List<Integer>>> temperatureAveragesPerLoopPerHr;

    public Logger() {
        this.totalTimeHeatingPerLoop = new HashMap<>();
        this.electricityUsedPerLoopPerHr = new HashMap<>();
        this.temperatureAveragesPerLoopPerHr = new HashMap<>();
    }

    public void addLoggedQTable(QTable table) {
        this.loggedQTable = table;
    }

    public void addToTotalTimeHeatingPerLoop(int loopNr, int time) {
        this.totalTimeHeatingPerLoop.put(loopNr, time);
    }

    public void addToElectricityUsedPerLoopPerHr(int loopNr, HashMap<Integer, Integer> map) {
        this.electricityUsedPerLoopPerHr.put(loopNr, map);
    }

    public void addToTemperatureAveragesPerLoopPerHr(int loopNr, HashMap<Integer, List<Integer>> map) {
        this.temperatureAveragesPerLoopPerHr.put(loopNr, map);
    }

    public QTable getLoggedQTable() {
        return loggedQTable;
    }

    public HashMap<Integer, Integer> getTotalTimeHeatingPerLoop() {
        return totalTimeHeatingPerLoop;
    }

    public HashMap<Integer, HashMap<Integer, Integer>> getElectricityUsedPerLoopPerHr() {
        return electricityUsedPerLoopPerHr;
    }

    public HashMap<Integer, HashMap<Integer, List<Integer>>> getTemperatureAveragesPerLoopPerHr() {
        return temperatureAveragesPerLoopPerHr;
    }
}
