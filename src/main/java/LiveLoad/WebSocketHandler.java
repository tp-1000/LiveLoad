package LiveLoad;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
//                        Matcher requestedPage = Pattern.compile("GET /([^.]*([^\\s]*))").matcher(data);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    private byte[] buildResponse(String contentType, String path) throws UnsupportedEncodingException {
//        byte[] body;
//        byte[] response;
//
//        //livewrapper is outside path
//        if(path.contains("liveWrapper")) {
//            body = getDataBody("/Users/thomaspetty/.liveload/livewrapper.js", contentType);
//        } else {
//            body = getDataBody(App.getDirectory() + path, contentType);
//        }
//
//        response = getHeaderAndBody(contentType, body);
//
//        return response;
//
//    }
//
//    private byte[] getHeaderAndBody(String contentType, byte[] body) throws UnsupportedEncodingException {
//        byte[] header;
//
//        //add content type to header
//        String contentLine = "Content-Type: text/" + contentType + "; charset=UTF-8\n";
//        if(! contentType.contains("html") || contentType.contains("css") || contentType.contains("js")){
//            //image
//            contentLine = "Content-Type: image/" + contentType + "\n";
//        }
//
//        //setting header
//        header = ("HTTP/1.1 200 Success!\n" +
//            "dev_env: left blank\n" +
//            contentLine +
//            body.length + '\n' +
//            "\n\n").getBytes(StandardCharsets.UTF_8);
//
//
//        // return header and body
//        try(ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream()) {
//            byteOutputStream.write(header);
//            byteOutputStream.write(body);
//            return byteOutputStream.toByteArray();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return ("HTTP/1.1 400 Bad Request\r\n").getBytes(StandardCharsets.UTF_8);
//    }
//
////
////    private byte[] getDataBody(String path, String contentType) {
////        byte[] body;
////        //check if html.. to inject
////        if (contentType.contains("html")) {
////            //inject
////
////        } else {
////            //get as data. if its text utf-8
////            //else byte[]
////        }
////
////
////
////        //else just take file and turn to byte[]
////        return body;
////    }
//
//
//////        String response = "HTTP/1.1 200 Success!\n" +
//////            "dev_env: left blank\n" +
//////            "Content-Type: text/" + contentType + "; charset=UTF-8\n" +
//////            body.length() + '\n' +
//////            "\n\n" +
//////            body;
//////
//////        String response2 = "HTTP/1.1 200 Success!\n" +
//////                "dev_env: left blank\n" +
//////                "Content-Type: image/" + contentType + "\n" +
//////                body.length() + '\n' +
//////                "\n\n" +
//////                body;
//////
//////        return response;
////    }
////
////    private byte[] headerToByteArray(String type, int bodyLength){
////        if(type.contains("css") || type.contains("html") || type.contains("js") ){
////            type.
////        }
////
////        byte[] header = "HTTP/1.1 200 Success!\n" +
////                "dev_env: left blank\n" +
////                "Content-Type: image/" + contentType + "\n" +
////                body.length() + '\n' +
////                "\n\n";
////        return header
////    }
////    private byte[] fileToDataBody(String path) {
////        //needs to be relative to a dir
////        //absolute path to dir is passed during launch
////        //hard code for now
//////
//////        BufferedImage bImage = ImageIO.read(new File("sample.jpg"));
//////        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//////        ImageIO.write(bImage, "jpg", bos );
//////        byte [] data = bos.toByteArray();
//////////////
//////        byte a[];
//////        byte b[];
//////
//////        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
//////        outputStream.write( a );
//////        outputStream.write( b );
//////
//////        byte c[] = outputStream.toByteArray( );
////
////        File file = new File(path);
////        try {
////            InputStream inputStream = new FileInputStream(file);
////            inputStream.close();
////
////            return data;
////        } catch (FileNotFoundException e) {
////            e.printStackTrace();
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////        return new File();
////    }
////
//    private byte[] fileToTextBody(String path, String fileType) throws IOException {
//        //needs to be relative to a dir
//        //absolute path to dir is passed during launch
//        //hard code for now
//        byte[] body;
//
//
//        //text
//        //if html
//        File file = new File(path);
//        if(fileType.contains("html") || fileType.contains("css") || fileType.contains("js")){
//            try {
//                if(fileType.contains("html")) {
//                    InputStream inputStream = new FileInputStream(file);
//                    String data = readFromInputStream(inputStream);
//                    inputStream.close();
//
//                    body = data.getBytes(StandardCharsets.UTF_8);
//                    return body;
//                }
//
//                //non-html request just read to byte[]
//                body = new FileInputStream(file).readAllBytes();
//                return body;
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            //image
//            BufferedImage bImage = ImageIO.read(file);
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            try {
//                ImageIO.write(bImage, fileType, bos );
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            body = bos.toByteArray();
//        }
//
//        body = "Content read Error".getBytes(StandardCharsets.UTF_8);
//        return body;
//    }
//
//
//    //if html read and append
//    private String readFromInputStream(InputStream inputStream) {
//        StringBuilder dataStringBuilder = new StringBuilder();
//
//        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                dataStringBuilder.append(line).append("\n");
//                if(line.toLowerCase().contains("<!doctype html>")){
//                    dataStringBuilder.append("<script src=\"liveWrapper.js\"></script>").append("\n");
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return dataStringBuilder.toString();
//    }


}