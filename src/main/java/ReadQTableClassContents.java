import logger.Logger;
import qLearning.QTable;

import java.io.*;
import java.util.Arrays;

public class ReadQTableClassContents {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        FileInputStream fi = new FileInputStream(new File("logfile.properties"));
        ObjectInputStream oi = new ObjectInputStream(fi);
        Logger logger = (Logger) oi.readObject();
        //QTable table = logger.getLoggedQTable();
        /*for (String key : table.getqTable().keySet()) {
            float[] tbl = table.getqTable().get(key);
            for (float val : tbl) {
                if (val != 0.0f) {
                    System.out.println(key + " " + Arrays.toString(tbl));
                }
            }
        }*/
        //System.out.println(logger.getElectricityUsedPerLoopPerHr());
        //System.out.println(logger.getTemperatureAveragesPerLoopPerHr());
        //System.out.println(logger.getTotalTimeHeatingPerLoop());
        //System.out.println(table.getAllEpisodeRewards());
        /*FileWriter fileWriter = new FileWriter("episodeRewards.txt");
        for (Integer key : table.getAllEpisodeRewards().keySet()) {
            float value = table.getAllEpisodeRewards().get(key);
            fileWriter.write(value + "\n");
        }
        fileWriter.close();*/
        //System.out.println(logger.getLoggedEnvironments().keySet().size());
        /*for (Integer key : logger.getLoggedEnvironments().keySet()) {
            System.out.println(logger.getLoggedEnvironments().get(key).getHeatingTimeAndPriceMap());
        }*/
    }
}
