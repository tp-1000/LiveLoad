package LiveLoad;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

//handles opening a port and subsequent connection requests are accepted and passed to the handler
public class WebSocketServer extends Thread {
    private ServerSocket serverSocket;
    private int port;
    private AtomicBoolean running = new AtomicBoolean(false);

    public WebSocketServer(int port) {
        this.port = port;
    }



    public void startServer() throws IOException {
        System.out.println("starting WS-Server. . .");
        serverSocket = new ServerSocket(port);
        this.start();
    }

    public void stopServer() {
        running.set(false);
        this.interrupt();
    }


    @Override
    public void run() {
        System.out.println(". . .running. . .");
        running.set(true);

        while(running.get()) {
            System.out.println("listening for a connection on 8080");
            //accept a connection and pass to handler
            try {
                Socket socket = serverSocket.accept();

                WebSocketHandler webSocketHandler = new WebSocketHandler(socket);
                webSocketHandler.start();
                System.out.println(WebSocketServer.activeCount());
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }
}
