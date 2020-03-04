import model.Model;
import org.concord.energy2d.model.Model2D;
import qLearning.QTable;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static void setupModel2D(Model2D model2D) {
        model2D.setTimeStep(1f);
        model2D.getThermostats().get(0).setDeadband(2);
        model2D.getThermostats().get(0).setSetPoint(20);
    }

    private static HashMap<String, float[]> setupQTable(float minInsideTemp, float maxInsideTemp, float minOutsideTemp, float maxOutsideTemp, int actionsLength) {
        QTable qTable = new QTable(minInsideTemp, maxInsideTemp, minOutsideTemp, maxOutsideTemp, actionsLength);
        return qTable.getqTable();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("I am main class!");
        float minOutsideTemp = -30f;
        float maxOutsideTemp = 40f;
        float minInsideTemp = 0f;
        float maxInsideTemp = 40f;
        int[] actions = new int[]{0, 1, 2};
        int actionsLength = actions.length;
        Model modelRunnable = new Model("src/main/resources/test-heating-sun-2.e2d");
        ExecutorService executor = Executors.newFixedThreadPool(1);

        Model2D model2D = modelRunnable.getModel2D();
        setupModel2D(model2D);
        HashMap<String, float[]> qTable = setupQTable(minInsideTemp, maxInsideTemp, minOutsideTemp, maxOutsideTemp, actionsLength);


        executor.execute(modelRunnable);

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //System.out.println(model2D.getThermostats().get(0).getThermometer().getCurrentData());
            //System.out.println(model2D.getTime());
        }
    }
}
