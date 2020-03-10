import math.Helpers;
import model.Model;
import org.concord.energy2d.event.ManipulationEvent;
import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.Thermometer;
import qLearning.Environment;
import qLearning.QTable;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static void setupModel2D(Model2D model2D) {
        model2D.setTimeStep(5f);
        model2D.getThermostats().get(0).setDeadband(1000);
        model2D.getThermostats().get(0).setSetPoint(1000);
    }

    private static QTable setupQTable(float minInsideTemp, float maxInsideTemp, float minOutsideTemp, float maxOutsideTemp, int actionsLength) {
        return new QTable(minInsideTemp, maxInsideTemp, minOutsideTemp, maxOutsideTemp, actionsLength);
    }

    private static Environment setupEnvironment(Model2D model2D, float targetTemp) {
        Thermometer insideThermometer = model2D.getThermometer("inside");
        Thermometer outsideThermometer = model2D.getThermometer("outside");
        float insideTemp;
        float outsideTemp;
        try {
            insideTemp = Helpers.roundFloat(insideThermometer.getCurrentData(), 1);
        } catch (Exception e) {
            insideTemp = 0.0f;
        }
        try {
            outsideTemp = Helpers.roundFloat(outsideThermometer.getCurrentData(), 1);
        } catch (Exception e) {
            outsideTemp = 0.0f;
        }
        return new Environment(outsideTemp, insideTemp, targetTemp);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("I am main class!");
        float minOutsideTemp = -30f;
        float maxOutsideTemp = 40f;
        float minInsideTemp = 0f;
        float maxInsideTemp = 40f;
        float targetTemp = 20f;
        Model modelRunnable = new Model("src/main/resources/test-heating-sun-2.e2d");
        ExecutorService executor = Executors.newFixedThreadPool(1);

        Model2D model2D = modelRunnable.getModel2D();
        setupModel2D(model2D);
        Environment environment = setupEnvironment(model2D, targetTemp);
        QTable qTable = setupQTable(minInsideTemp, maxInsideTemp, minOutsideTemp, maxOutsideTemp, environment.getActionSpace().length);

        executor.execute(modelRunnable);
        float prevTime = 0;

        while (true) {
            try {
                Thread.sleep(1);
                model2D.stop();
                float time = model2D.getTime();
                if ((time % 1800 <= 10 & time > prevTime + 15) & time < 43200) {
                    qTable.calculateQTableValue(environment, model2D);
                    prevTime = time;
                }
                if (time >= 43200) {
                    qTable.calculateQTableValue(environment, model2D);
                    qTable.startNewIteration();
                    model2D.reset();
                    environment = setupEnvironment(model2D, targetTemp);
                    prevTime = 0;
                }
                executor.execute(modelRunnable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
