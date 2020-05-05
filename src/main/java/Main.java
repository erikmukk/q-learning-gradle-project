public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            System.out.println("I am main class!");
            String filename = args[0];
            float epsilon = Float.parseFloat(args[1]);
            float epsilonDecay = Float.parseFloat(args[2]);
            float learningRate = Float.parseFloat(args[3]);
            float discount = Float.parseFloat(args[4]);
            int loopsCount = Integer.parseInt(args[5]);
            float temperatureRewardWeight = Float.parseFloat(args[6]);
            float electricityRewardWeight = Float.parseFloat(args[7]);
            int loopLengthMins = Integer.parseInt(args[8]);
            float targetTemp = Float.parseFloat(args[9]);
            float initialTemp = Float.parseFloat(args[10]);
            /*String filename = "debug";
            float epsilon = 0.9f;
            float epsilonDecay = 0.999f;
            float learningRate = 0.1f;
            float discount = 0.9f;
            int loopsCount = 2500;
            float temperatureRewardWeight = 0.9f;
            float electricityRewardWeight = 0.1f;
            int loopLengthMins = 1;
            float targetTemp = 22f;
            float initialTemp = 15f;*/
            new Observer(filename, epsilon, epsilonDecay, learningRate, discount, loopsCount, temperatureRewardWeight, electricityRewardWeight, loopLengthMins, targetTemp, initialTemp);
        } else {
            System.out.println("Give me arguments. Exiting...");
        }
    }
}
