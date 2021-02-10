package LiveLoad;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.WebSocket;

//handles opening a port and subsequent connection requests are accepted and passed to the handler
public class WebSocketServer extends Thread {
    private ServerSocket serverSocket;
    private int port;
    private boolean running = false;

    public WebSocketServer(int port) {
        this.port = port;
    }

    public void startServer() throws IOException {
        System.out.println("starting WS-Server. . .");
        serverSocket = new ServerSocket(port);
        this.start();
    }

    public void stopServer() {
        running = false;
        this.interrupt();
    }

    @Override
    public void run() {
        System.out.println(". . .running. . .");
        running = true;
        while(running) {
            System.out.println("listening for a connection");
            System.out.println("a loop");
            //accept a connection and pass to hand off
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
