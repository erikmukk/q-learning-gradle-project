import logger.Logger;
import qLearning.QTable;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ReadQTableClassContents {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        FileInputStream fi = new FileInputStream(new File("logfile-inside-out-23-03-2020.properties"));
        ObjectInputStream oi = new ObjectInputStream(fi);
        Logger logger = (Logger) oi.readObject();

        QTable table = logger.getLoggedQTable();
        /*for (String key : table.getqTable().keySet()) {
            float[] tbl = table.getqTable().get(key);
            for (float val : tbl) {
                if (val != 0.0f) {
                    System.out.println(key + " " + Arrays.toString(tbl));
                }
            }
        }*/
        FileWriter fileWriter = new FileWriter("episodeRewards-23-03-2020.txt");
        for (Integer key : table.getAllEpisodeRewards().keySet()) {
            float value = table.getAllEpisodeRewards().get(key);
            fileWriter.write(value + "\n");
        }
        fileWriter.close();

        HashMap<Integer, HashMap<Integer, Integer>> electricityUsedPerLoopPerHr = logger.getElectricityUsedPerLoopPerHr();
        FileWriter fileWriter2 = new FileWriter("electricityUsedPerLoopPerHr-23-03-2020.txt");
        fileWriter2.write("{"+ "\n");
        int keysetSize = electricityUsedPerLoopPerHr.keySet().size();
        for (Integer key : electricityUsedPerLoopPerHr.keySet()) {
            String outputString = "\"" + key.toString() + "\":";
            if (electricityUsedPerLoopPerHr.get(key).toString().length() > 2) {
                String dictString = electricityUsedPerLoopPerHr.get(key).toString()
                        .replace("=", "\":\"")
                        .replace(", ", "\",\"")
                        .replace("{", "{\"")
                        .replace("}", "\"}");
                outputString += dictString;
            } else {
                outputString += electricityUsedPerLoopPerHr.get(key).toString();
            }
            if (key != keysetSize) {
                outputString += ",";
            }
            fileWriter2.write(outputString + "\n");
        }
        fileWriter2.write("}");
        fileWriter2.close();

        HashMap<Integer, HashMap<Integer, List<Integer>>> tempAveragesPerLoopPerHr = logger.getTemperatureAveragesPerLoopPerHr();
        FileWriter fileWriter4 = new FileWriter("tempAveragesPerLoopPerHr-23-03-2020.txt");
        fileWriter4.write("{"+ "\n");
        int keysetSize2 = tempAveragesPerLoopPerHr.keySet().size();
        for (Integer key : tempAveragesPerLoopPerHr.keySet()) {
            String outputString = "\"" + key.toString() + "\":";
            if (tempAveragesPerLoopPerHr.get(key).toString().length() > 2) {
                String dictString = tempAveragesPerLoopPerHr.get(key).toString()
                        .replace("=", "\":\"")
                        .replace(", ", "\",\"")
                        .replace("{", "{\"")
                        .replace("}", "\"}");
                outputString += dictString;
            } else {
                outputString += tempAveragesPerLoopPerHr.get(key).toString();
            }
            if (key != keysetSize2) {
                outputString += ",";
            }
            fileWriter4.write(outputString + "\n");
        }
        fileWriter4.write("}");
        fileWriter4.close();

        HashMap<Integer, Integer> totalTimeHeatingPerLoop = logger.getTotalTimeHeatingPerLoop();
        FileWriter fileWriter3 = new FileWriter("totalTimeHeatingPerLoop-23-03-2020.txt");
        for (Integer key : totalTimeHeatingPerLoop.keySet()) {
            float value = totalTimeHeatingPerLoop.get(key);
            fileWriter3.write(value + "\n");
        }
        fileWriter3.close();
    }
}
