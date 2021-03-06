package LiveLoad;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    private static String directory = "/Users/thomaspetty/Desktop/";

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Live-reloading that listens for localhost:8080");
            System.out.println("    Use:     $thisApp <directory to watch>");
            System.out.println("    Example: $thisApp ~/Users/someUser/Desktop/");
            System.exit(0);
        } else {
            directory = args[0];
            if(! Files.isDirectory(Paths.get(directory))) {
                System.out.println("Please provide a valid directory to watch");
                System.out.println("Provided invalid: " + directory);
                System.exit(0);
            }

            System.out.println("Your working directory: " + directory);
            WebSocketServer webSocketServer = new WebSocketServer(8080);
            webSocketServer.startServer();
        }
    }

    public static String getDirectory() {
        return directory;

    }

}