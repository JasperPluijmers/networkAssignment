import client.Client;
import server.Guard;

public class Main {

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("-c")) {
            Client client = new Client();
        } else {
            Guard guard = new Guard(3651);
        }
    }
}
