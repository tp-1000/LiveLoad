package LiveLoad;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSocketHandler extends Thread {
    private Socket socket;

    WebSocketHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try (
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                Scanner s = new Scanner(in, "UTF-8");
        ) {
            System.out.println(" ** received a connection");
            try {
                String data = s.useDelimiter("\\r\\n\\r\\n").next();
                Matcher get = Pattern.compile("^GET").matcher(data);

                if (get.find()) {
                    //if webSocket request
                    Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                    if (match.find()) {
                        byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                                + "Connection: Upgrade\r\n"
                                + "Upgrade: websocket\r\n"
                                + "Sec-WebSocket-Accept: "
                                + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
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
//                        Matcher requestedPage = Pattern.compile("GET /([^.]*([^\\s]*))").matcher(data);
                        Matcher requestedPage = Pattern.compile("GET /([^.]*\\.([^\\s]*))").matcher(data);
//                        needs to handle requests with and add back on the ? mine type to the text/css
//                        href="../out.css?v=1.0"


//                        System.out.println(data);
                        if(requestedPage.find()) {

    //                          build a response
                            String filePathString = requestedPage.group(1);
                            String fileType = requestedPage.group(2);
                            byte[] response = buildResponse(fileType, filePathString).getBytes("UTF-8");
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
                    byte[] response = ("HTTP/1.1 400 Bad Request\r\n").getBytes("UTF-8");
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String buildResponse(String contentType, String path) {
        String body;
        if (path.contains("liveWrapper")) {
            body = fileToBody("/Users/thomaspetty/.liveload/livewrapper.js");
        } else {
            body = fileToBody(App.getDirectory() + path);
        }

        String response = "HTTP/1.1 200 Success!\n" +
            "dev_env: left blank\n" +
            "Content-Type: text/" + contentType + "; charset=UTF-8\n" +
            body.length() + '\n' +
            "\n\n" +
            body;

        return response;
    }

    private String fileToBody(String path) {
        //needs to be relative to a dir
        //absolute path to dir is passed during launch
        //hard code for now

        File file = new File(path);
        try {
            InputStream inputStream = new FileInputStream(file);
            String data = readFromInputStream(inputStream);
            inputStream.close();

            return data;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Content read Error";
    }

    private String readFromInputStream(InputStream inputStream) {
        StringBuilder dataStringBuilder = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                dataStringBuilder.append(line).append("\n");
                if(line.toLowerCase().contains("<!doctype html>")){
                    dataStringBuilder.append("<script src=\"liveWrapper.js\"></script>").append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataStringBuilder.toString();
    }


}