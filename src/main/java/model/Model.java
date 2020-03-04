package model;

import org.concord.energy2d.model.Model2D;
import org.concord.energy2d.system.XmlDecoderHeadlessForModelExport;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Model implements Runnable {
    Model2D model2D;

    public Model(String e2dFileName) throws IOException {
        this.model2D = new Model2D();
        loadModel(e2dFileName);
    }

    public void loadModel(String e2dFileName) throws IOException {
        InputStream is = new FileInputStream(e2dFileName);
        DefaultHandler saxHandler = new XmlDecoderHeadlessForModelExport(this.model2D);
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(new InputSource(is), saxHandler);
        } catch (SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public Model2D getModel2D() {
        return model2D;
    }

    public void stop() {
        this.model2D.stop();
    }

    @Override
    public void run() {
        this.model2D.run();
    }
}
