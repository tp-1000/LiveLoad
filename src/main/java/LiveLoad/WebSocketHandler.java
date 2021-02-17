package LiveLoad;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//processes GET request -- websocket & browser data
public class WebSocketHandler implements Runnable {
    private Socket socket;
    private String data;
            
    WebSocketHandler(Socket socket) {
        this.socket = socket;
    }


    public void run() {
        //setup socket communication
        try (
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                Scanner scanner = new Scanner(in, "UTF-8");
        ) {
            System.out.println(" ** received a connection");
            data = scanner.useDelimiter("\\r\\n\\r\\n").next();
            Matcher get = Pattern.compile("^GET").matcher(data);

            //If GET request proceed to process
            if(get.find()){
                //needs to correctly respond to:
                //websocket + text/image
                Response response = new Response(data);
                out.write(response.getResponse(), 0, response.getResponse().length);

                if(data.toLowerCase().contains("websocket")){
                    DirWatch dirWatch = new DirWatch(App.getDirectory());
                    dirWatch.run();

                }

            } else {
                Response response = new Response(400);
                out.write(response.badRequest());

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}