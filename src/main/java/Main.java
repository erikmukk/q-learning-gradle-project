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
            new Observer(filename, epsilon, epsilonDecay, learningRate, discount, loopsCount, temperatureRewardWeight, electricityRewardWeight);
        } else {
            System.out.println("Give me arguments. Exiting...");
        }
    }
}
