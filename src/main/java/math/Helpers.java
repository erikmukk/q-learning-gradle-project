package math;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Helpers {

    public static float roundFloat(float value, int places) {
        BigDecimal bd = new BigDecimal(Float.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }
}
