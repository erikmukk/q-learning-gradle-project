import com.fasterxml.jackson.databind.ObjectMapper;
import logger.Logger;
import qLearning.QTable;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadQTableClassContents {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        FileInputStream fi = new FileInputStream(new File("logfile-27-03-2020.properties"));
        ObjectInputStream oi = new ObjectInputStream(fi);
        ObjectMapper mapper = new ObjectMapper();
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

        Map<Integer, Float> rewards = table.getAllEpisodeRewards();
        try {
            mapper.writeValue(new File("rewards-27-03-2020.json"), rewards);
        } catch (Exception e) {
            e.printStackTrace();
        }


        HashMap<Integer, HashMap<Integer, Integer>> electricityUsedPerLoopPerHr = logger.getElectricityUsedPerLoopPerHr();
        try {
            mapper.writeValue(new File("electricityUsed-27-03-2020.json"), electricityUsedPerLoopPerHr);
        } catch (Exception e) {
            e.printStackTrace();
        }



        HashMap<Integer, HashMap<Integer, List<Float>>> tempAveragesPerLoopPerHr = logger.getTemperatureAveragesPerLoopPerHr();
        try {
            mapper.writeValue(new File("tempAverages-27-03-2020.json"), tempAveragesPerLoopPerHr);
        } catch (Exception e) {
            e.printStackTrace();
        }


        HashMap<Integer, Integer> totalTimeHeatingPerLoop = logger.getTotalTimeHeatingPerLoop();
        try {
            mapper.writeValue(new File("totalTimeHeating-27-03-2020.json"), totalTimeHeatingPerLoop);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
