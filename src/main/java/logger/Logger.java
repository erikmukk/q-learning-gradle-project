package logger;

import qLearning.Environment;
import qLearning.QTable;

import java.io.Serializable;
import java.util.HashMap;

public class Logger implements Serializable {

    HashMap<Integer, Environment> loggedEnvironments;
    QTable loggedQTable;

    public Logger() {
        this.loggedEnvironments = new HashMap<>();
    }

    public void addToLoggedEnvironments(Environment env, int loopNr) {
        this.loggedEnvironments.put(loopNr, env);
    }

    public void addLoggedQTable(QTable table) {
        this.loggedQTable = table;
    }

    public HashMap<Integer, Environment> getLoggedEnvironments() {
        return loggedEnvironments;
    }

    public QTable getLoggedQTable() {
        return loggedQTable;
    }
}
