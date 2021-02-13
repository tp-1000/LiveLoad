package LiveLoad;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
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

    @Override
    public void run() {
        //setup socket communication
        try (
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                Scanner scanner = new Scanner(in, "UTF-8");
        ) {
            System.out.println(" ** received a connection");
            data = scanner.useDelimiter("\\r\\n\\r\\n").next();
            scanner.close();
            Matcher get = Pattern.compile("^GET").matcher(data);

            //If GET request proceed to process
            if(get.find()){
                System.out.println("Get received");
            } else {
                System.out.println(404);
            }

/*

            try {
                String data = scanner.useDelimiter("\\r\\n\\r\\n").next();
                Matcher get = Pattern.compile("^GET").matcher(data);

                if (get.find()) {
                    //if webSocket request
                    Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                    if (match.find()) {
                        byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                                + "Connection: Upgrade\r\n"
                                + "Upgrade: websocket\r\n"
                                + "Sec-WebSocket-Accept: "
                                + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.UTF_8)))
                                + "\r\n\r\n").getBytes("UTF-8");
                        out.write(response, 0, response.length);
                            DirWatch dirWatch = new DirWatch(socket);
                            dirWatch.start(); //start() makes now thread.out.println("blocking");
                            while(socket.isConnected()){
                            }
                        out.flush();
                        out.close();
                        in.close();

                    } else {
                        //respond with the requested page
//                            this.body = body;
//                            this.contentType = "Content-Type: text/" + type + "; charset=UTF-8";
//                            this.bodyLength = "Content-Length: " + body.length();

//                                   GET /pub/WWW/TheProject.html HTTP/1.1
                        //       Host: www.w3.org
//                        Matcher requestedPage = Pattern.compile("GET /([^.]*([^\\scanner]*))").matcher(data);
                        Matcher requestedPage = Pattern.compile("GET /([^.]*\\.([^\\s]*))").matcher(data);
//                        needs to handle requests with and add back on the ? mine type to the text/css
//                        href="../out.css?v=1.0"


//                        System.out.println(data);
                        if(requestedPage.find()) {

    //                          build a response
                            String filePathString = requestedPage.group(1);
                            String fileType = requestedPage.group(2);
                            byte[] response = new Response(fileType, App.getDirectory() + filePathString).getResponse();

//                            byte[] response = buildResponse(fileType, filePathString);
                            out.write(response);
                            out.flush();
                            out.close();
                            in.close();
                            socket.close();
                        }
                        out.flush();
                        out.close();
                        in.close();
                        socket.close();
                    }
                } else {

                    String fourhundered = "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: 122\r\n" +
                        "Connection: keep-alive\r\n" +
                            "\r\n\r\n" +
                            "<html>" +
                            "<head><title>404 Not Found</title></head>" +
                            "<body bgcolor='white'>" +
                            "<center><h1>404 Not Found</h1></center>" +
                            "</body>" +
                            "</html>";

                    byte[] response = fourhundered.getBytes(StandardCharsets.UTF_8);
                    out.write(response, 0, response.length);
                    System.out.println("GET requests only");
                    out.flush();
                    out.close();
                    this.socket.close();
                }


            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
    */
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}