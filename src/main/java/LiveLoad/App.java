/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package LiveLoad;

import java.io.IOException;

public class App {
    public String getGreeting() {
        return "Hello world.";
    }

    public static void main(String[] args) throws IOException {
        DirWatch dirWatch = new DirWatch("/Users/thomaspetty/Desktop");
        dirWatch.start(); //start() makes now thread

        WebSocketServer webSocketServer = new WebSocketServer(8080);
        webSocketServer.startServer();

        System.out.println(new App().getGreeting());
    }
}