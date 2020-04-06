import com.fasterxml.jackson.databind.ObjectMapper;
import logger.Logger;
import math.Helpers;
import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.model.Thermometer;
import org.concord.energy2d.system.XmlDecoderHeadlessForModelExport;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import qLearning.Environment;
import qLearning.QTable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Observer implements PropertyChangeListener {

    public Environment environment;
    public QTable qTable;
    public Logger logger;
    public Model2D model2D;
    public float targetTemp;
    public String logfileName;
    public String envfileName;
    public int loopLengthMins = 10;
    public String filenameBase;
    public float epsilon;
    public float epsilonDecay;
    public float learningRate;
    public float discount;

    public Observer(String filenameBase, float epsilon, float epsilonDecay, float learningRate, float discount) throws Exception {
        this.model2D = new Model2D();
        this.model2D.addChangeListener(this);
        this.filenameBase = filenameBase;
        this.epsilon = epsilon;
        this.epsilonDecay = epsilonDecay;
        this.learningRate = learningRate;
        this.discount = discount;
        init();
    }

    private void setupModel2D() {
        this.model2D.setTimeStep(10f);
        this.model2D.getThermostats().get(0).setDeadband(200f);
        this.model2D.getThermostats().get(0).setSetPoint(200f);
    }

    private void setupQTable(float minInsideTemp, float maxInsideTemp, float minOutsideTemp, float maxOutsideTemp, int actionsLength, float maxElectricityPrice) {
        this.qTable = new QTable(minInsideTemp, maxInsideTemp, minOutsideTemp, maxOutsideTemp, actionsLength, maxElectricityPrice, this.epsilon, this.epsilonDecay, this.learningRate, this.discount);
    }

    private void setupQTable(String filename) throws Exception {
        FileInputStream fi = new FileInputStream(new File(filename));
        ObjectInputStream oi = new ObjectInputStream(fi);
        Logger log = (Logger) oi.readObject();
        this.qTable = log.getLoggedQTable();
    }

    private void setupEnvironment(float targetTemp) {
        Thermometer insideThermometer = this.model2D.getThermometer("inside");
        Thermometer outsideThermometer = this.model2D.getThermometer("outside");
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
        this.environment = new Environment(outsideTemp, insideTemp, targetTemp, this.loopLengthMins);
    }

    public void init() throws Exception {
        this.logfileName = "logfile.properties";
        //this.envfileName = "logfile-03-04-2020-6.properties";
        this.logger = new Logger();
        float minOutsideTemp = -30f;
        float maxOutsideTemp = 40f;
        float minInsideTemp = 0f;
        float maxInsideTemp = 40f;
        this.targetTemp = 20f;
        InputStream is = new FileInputStream("src/main/resources/test-heating-sun-2.e2d");
        DefaultHandler saxHandler = new XmlDecoderHeadlessForModelExport(this.model2D);
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(new InputSource(is), saxHandler);
        } catch (SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        setupModel2D();
        setupEnvironment(this.targetTemp);

        HashMap<Integer, Float> electricityStockPrice = this.environment.getElectricityStockPrice();
        float maxElectricityValue = 0;
        for (Map.Entry<Integer, Float> entry : electricityStockPrice.entrySet()) {
            if (entry.getValue() > maxElectricityValue) {
                maxElectricityValue = entry.getValue();
            }
        }
        setupQTable(minInsideTemp, maxInsideTemp, minOutsideTemp, maxOutsideTemp, this.environment.getActionSpace().length, maxElectricityValue);

        System.out.println("QTable initialized");
        this.qTable.doStepBeforeRunningOneMinute(this.environment, this.model2D);
        this.model2D.run();
    }

    private HashMap<String, String> makeInfoMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("filenameBase", this.filenameBase);
        map.put("epsilon", String.valueOf(this.epsilon));
        map.put("epsilonDecay", String.valueOf(this.epsilonDecay));
        map.put("learningRate", String.valueOf(this.learningRate));
        map.put("discount", String.valueOf(this.discount));
        return map;
    }

    private void writeIntoFile(Logger logger) {
        ObjectMapper mapper = new ObjectMapper();
        QTable table = logger.getLoggedQTable();
        Map<Integer, Float> rewards = table.getAllEpisodeRewards();
        HashMap<Integer, HashMap<Integer, Integer>> electricityUsedPerLoopPerHr = logger.getElectricityUsedPerLoopPerHr();
        HashMap<Integer, HashMap<Integer, List<Float>>> tempAveragesPerLoopPerHr = logger.getTemperatureAveragesPerLoopPerHr();
        HashMap<Integer, Integer> totalTimeHeatingPerLoop = logger.getTotalTimeHeatingPerLoop();
        Map<String, String> infoMap = makeInfoMap();
        String rewardFileName = "rewards.json";
        String electricityFileName = "electricityUsed.json";
        String tempFileName = "tempAverages.json";
        String timeFileName = "totalTimeHeating.json";
        String infoFileName = "info.json";
        String targetFolder = "testResults/" + this.filenameBase;
        infoMap.put("rewardFileName", rewardFileName);
        infoMap.put("electricityFileName", electricityFileName);
        infoMap.put("tempFileName", tempFileName);
        infoMap.put("timeFileName", timeFileName);
        try {
            File file = new File(targetFolder);
            file.mkdir();
            mapper.writeValue(new File(targetFolder + "/" + rewardFileName), rewards);
            mapper.writeValue(new File(targetFolder + "/"  + electricityFileName), electricityUsedPerLoopPerHr);
            mapper.writeValue(new File(targetFolder + "/"  + tempFileName), tempAveragesPerLoopPerHr);
            mapper.writeValue(new File(targetFolder + "/"  + timeFileName), totalTimeHeatingPerLoop);
            mapper.writeValue(new File(targetFolder + "/"  + infoFileName), infoMap);
            FileOutputStream f = new FileOutputStream(new File(targetFolder + "/"  + this.filenameBase + ".properties"));
            ObjectOutputStream o = new ObjectOutputStream(f);
            o.writeObject(logger);
            o.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void calculateValues(float targetTemp) {
        float time = this.model2D.getTime();
        if (time % (this.loopLengthMins*60) == 0 & time < 86400) {
            this.qTable.doWhenXTimeHasPassed(this.environment, this.model2D);
            this.qTable.doStepBeforeRunningOneMinute(this.environment, this.model2D);
        }
        if (time >= 86400) {
            this.qTable.doWhenXTimeHasPassed(this.environment, this.model2D);
            this.qTable.startNewIteration(this.logger, this.environment);
            this.model2D.reset();
            setupEnvironment(targetTemp);
            int loops = this.qTable.getLoops();
            if (loops % 1 == 0) {
                writeIntoFile(this.logger);
                System.out.println("1000 iterations added!\t" + this.qTable.getLoops() + "loops completed");
                System.exit(0);
            }
            this.qTable.doStepBeforeRunningOneMinute(this.environment, this.model2D);
        }
        this.model2D.resume();
    }
    public void calculateValuesForTest(float targetTemp) {
        float time = this.model2D.getTime();
        if (time % (this.loopLengthMins*60) == 0 & time < 86400) {
            this.qTable.doWhenXTimeHasPassedForOneIterationTest(this.environment, this.model2D);
        }
        if (time >= 86400) {
            this.qTable.doWhenXTimeHasPassedForOneIterationTest(this.environment, this.model2D);
            this.qTable.endTestIteration(this.logger, this.environment);
            writeIntoFile(this.logger);
            System.out.println("Testing finished");
            System.exit(0);
        }
        this.model2D.resume();
    }

    // After every minute, program is stopped
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        this.model2D.stop();
        try {
            calculateValues(this.targetTemp);
            //calculateValuesForTest(this.targetTemp);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
