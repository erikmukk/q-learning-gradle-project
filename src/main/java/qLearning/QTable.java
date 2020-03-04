package qLearning;

import math.Helpers;

import java.util.HashMap;

public class QTable {

    HashMap<String, float[]> qTable;

    public QTable(float minInsideTemp, float maxInsideTemp, float minOutsideTemp, float maxOutsideTemp, int actionsLength) {
        initQTable(minInsideTemp, maxInsideTemp, minOutsideTemp, maxOutsideTemp, actionsLength);
    }

    private void initQTable(float minInsideTemp, float maxInsideTemp, float minOutsideTemp, float maxOutsideTemp, int actionsLength) {
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
        // sisetemp_välistemparatuur
        //String belowMinTempKey = "-_" + ""
    }

    public String makeQTableKey(float insideTemp, float outsideTemp) {
        return insideTemp + "_" + outsideTemp;
    }

    public HashMap<String, float[]> getqTable() {
        return qTable;
    }
}
