import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import logger.Logger;
import org.concord.energy2d.model.Model2D;
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
    public int loopLengthMins = 1;
    public String filenameBase;
    public String qTableFilename;
    public float deadband;

    public Observer(String filenameBase, String qTableFilename) throws Exception {
        this.model2D = new Model2D();
        this.model2D.addChangeListener(this);
        this.filenameBase = filenameBase;
        this.qTableFilename = qTableFilename;
        init();
    }

    private void setupModel2D() {
        this.model2D.setTimeStep(10f);
        this.model2D.getThermostats().get(0).setDeadband(200f);
        this.model2D.getThermostats().get(0).setSetPoint(200f);
    }

    private void setupQTable() {
        this.qTable = new QTable(this.targetTemp, this.deadband);
    }

    private void setupEnvironment(float targetTemp) {
        float bgTemp = model2D.getBackgroundTemperature();
        this.environment = new Environment(bgTemp, bgTemp, targetTemp, this.loopLengthMins);
    }

    public void init() throws Exception {
        this.logger = new Logger();
        this.targetTemp = 20f;
        this.deadband = 1f;
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
        setupQTable();
        System.out.println("QTable loaded");
        this.model2D.takeMeasurement();
        this.qTable.doStepBeforeRunningXMinutes(this.environment, this.model2D);
        this.model2D.run();
    }

    private HashMap<String, String> makeInfoMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("filenameBase", this.filenameBase);
        return map;
    }

    private void writeIntoFile(Logger logger) {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<Integer, HashMap<Integer, Integer>> electricityUsedPerLoopPerHr = logger.getElectricityUsedPerLoopPerHr();
        HashMap<Integer, HashMap<Integer, List<Float>>> tempAveragesPerLoopPerHr = logger.getTemperatureAveragesPerLoopPerHr();
        HashMap<Integer, Integer> totalTimeHeatingPerLoop = logger.getTotalTimeHeatingPerLoop();
        Map<String, String> infoMap = makeInfoMap();
        String electricityFileName = "electricityUsed.json";
        String tempFileName = "tempAverages.json";
        String timeFileName = "totalTimeHeating.json";
        String infoFileName = "info.json";
        String targetFolder = "testResults/" + this.filenameBase;
        infoMap.put("electricityFileName", electricityFileName);
        infoMap.put("tempFileName", tempFileName);
        infoMap.put("timeFileName", timeFileName);
        try {
            File file = new File(targetFolder);
            file.mkdir();
            mapper.writeValue(new File(targetFolder + "/"  + electricityFileName), electricityUsedPerLoopPerHr);
            mapper.writeValue(new File(targetFolder + "/"  + tempFileName), tempAveragesPerLoopPerHr);
            mapper.writeValue(new File(targetFolder + "/"  + timeFileName), totalTimeHeatingPerLoop);
            mapper.writeValue(new File(targetFolder + "/"  + infoFileName), infoMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void calculateValues(float targetTemp) {
        float time = this.model2D.getTime();
        this.model2D.takeMeasurement();
        if (time % (this.loopLengthMins*60) == 0 & time < 86400) {
            // Take new action before running x time
            this.qTable.doStepBeforeRunningXMinutes(this.environment, this.model2D);
        }
        if (time >= 86400) {
            this.qTable.startNewIteration(this.logger, this.environment);
            setupEnvironment(targetTemp);
            writeIntoFile(this.logger);
            System.out.println("END");
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
