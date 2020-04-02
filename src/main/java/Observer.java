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
import java.util.Map;

public class Observer implements PropertyChangeListener {

    public Environment environment;
    public QTable qTable;
    public Logger logger;
    public Model2D model2D;
    public float targetTemp;
    public String logfileName;
    public int loopLengthMins = 1;

    public Observer() throws Exception {
        this.model2D = new Model2D();
        this.model2D.addChangeListener(this);
        init();
    }

    private void setupModel2D() {
        //this.model2D.setTimeStep(5f);
        this.model2D.setTimeStep(10f);
        this.model2D.getThermostats().get(0).setDeadband(200f);
        this.model2D.getThermostats().get(0).setSetPoint(200f);
    }

    private void setupQTable(float minInsideTemp, float maxInsideTemp, float minOutsideTemp, float maxOutsideTemp, int actionsLength, float maxElectricityPrice) {
        this.qTable = new QTable(minInsideTemp, maxInsideTemp, minOutsideTemp, maxOutsideTemp, actionsLength, maxElectricityPrice);
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
        try {
            setupQTable(this.logfileName);
        } catch (Exception e) {
            HashMap<Integer, Float> electricityStockPrice = this.environment.getElectricityStockPrice();
            float maxElectricityValue = 0;
            for (Map.Entry<Integer, Float> entry : electricityStockPrice.entrySet()) {
                if (entry.getValue() > maxElectricityValue) {
                    maxElectricityValue = entry.getValue();
                }
            }
            setupQTable(minInsideTemp, maxInsideTemp, minOutsideTemp, maxOutsideTemp, this.environment.getActionSpace().length, maxElectricityValue);
        }
        System.out.println("QTable initialized");
        this.qTable.doStepBeforeRunningOneMinute(this.environment, this.model2D);
        this.model2D.run();
    }

    private void writeIntoFile(Serializable serializable, String filename) throws IOException {
        FileOutputStream f = new FileOutputStream(new File(filename));
        ObjectOutputStream o = new ObjectOutputStream(f);
        o.writeObject(serializable);
        o.close();
    }

    public void calculateValues(float targetTemp, String logfileName) throws IOException {
        float time = this.model2D.getTime();
        if (time % (this.loopLengthMins*60) == 0 & time < 86400 & time != 0) {
            this.qTable.doWhenXTimeHasPassed(this.environment, this.model2D);
            this.qTable.doStepBeforeRunningOneMinute(this.environment, this.model2D);
        }
        if (time >= 86400) {
            this.qTable.doWhenXTimeHasPassed(this.environment, this.model2D);
            this.qTable.startNewIteration(this.logger, this.environment);
            this.model2D.reset();
            setupEnvironment(targetTemp);
            int loops = this.qTable.getLoops();
            if (loops % 50 == 0) {
                writeIntoFile(this.logger, logfileName);
                System.out.println("50 iterations added!\t" + this.qTable.getLoops() + "loops completed");
            }
            this.qTable.doStepBeforeRunningOneMinute(this.environment, this.model2D);
        }
        this.model2D.resume();
    }
    public void calculateValuesForTest(float targetTemp, String logfileName) throws IOException {
        float time = this.model2D.getTime();
        if (time % (this.loopLengthMins*60) == 0 & time < 86400) {
            this.qTable.doWhenXTimeHasPassedForOneIterationTest(this.environment, this.model2D);
        }
        if (time >= 86400) {
            this.qTable.doWhenXTimeHasPassedForOneIterationTest(this.environment, this.model2D);
            this.qTable.endTestIteration(this.logger, this.environment);
            writeIntoFile(this.logger, logfileName);
            System.out.println("Testing finished");
            System.exit(0);
        }
    }

    // After every minute, program is stopped
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        this.model2D.stop();
        try {
            calculateValues(this.targetTemp, this.logfileName);
            //calculateValuesForTest(this.targetTemp, this.logfileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
