package math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class Helpers {

    public static float roundFloat(float value, int places) {
        BigDecimal bd = new BigDecimal(Float.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    public static double roundDouble(double value, int places) {
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static boolean getRandomBoolean() {
        return new Random().nextBoolean();
    }
}
