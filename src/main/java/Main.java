public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("Main");
        if (args.length > 0) {
            System.out.println("I am main class!");
            String filename = args[0];
            String qTableFilename = args[1];
            Float targetTemp = Float.parseFloat(args[2]);
            Float deadband = Float.parseFloat(args[3]);
            new Observer(filename, qTableFilename, targetTemp, deadband);
        } else {
            System.out.println("Give me arguments. Exiting...");
        }
    }
}
