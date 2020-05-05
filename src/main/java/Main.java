public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("Main");
        if (args.length > 0) {
            System.out.println("I am main class!");
            //String filename = args[0];
            //String qTableFilename = args[1];
            //float targetTemp = Float.parseFloat(args[2]);
            //int timestep = Integer.parseInt(args[3]);
            String filename = "05-05-2020-electricity-1-01-debug";
            String qTableFilename = "05-05-2020-electricity-1-01/qTable.json";
            float targetTemp = 22f;
            int timestep = 10;
            new Observer(filename, qTableFilename, targetTemp, timestep);
        } else {
            System.out.println("Give me arguments. Exiting...");
        }
    }
}
