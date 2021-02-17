package LiveLoad;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Response {

    private byte[] body = new byte[0];
    private byte[] header;
    private String dataIn;

    //header
    //content type
    //text/javascript;charset=UTF-8
    //text/css;charset=UTF-8
    //text/html;charset=UTF-8
    //image/*
    //"Content-Length: 122\r\n"
    //"Content-Type: text/html\r\n"
    private String contentType;
    private String contentLength; 


    Pattern pathFrom = Pattern.compile("");

    Pattern typeAndPathFrom = Pattern.compile("GET /([^.]*([^\\s]*))");
    Pattern webSocketFrom = Pattern.compile("Sec-WebSocket-Key: (.*)");
    Pattern getFrom = Pattern.compile("^GET");



    public Response(int statusCode){
        //TODO make constructor to error status codes
        //makes new  request object
    }

//    public static void main(String[] args) {
//        ServerSocket server = null;
//        try {
//            server = new ServerSocket(8081);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            System.out.println("Server has started on 127.0.0.1:80.\r\nWaiting for a connection...");
//            Socket client = server.accept();
//            System.out.println("A client connected.");
//            InputStream in = client.getInputStream();
//            OutputStream out = client.getOutputStream();
//            Scanner s = new Scanner(in, "UTF-8");
//            try {
//                String data = s.useDelimiter("\\r\\n\\r\\n").next();
//                Matcher get = Pattern.compile("^GET").matcher(data);
//                if (get.find()) {
//                    Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
//                    match.find();
//                    byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
//                            + "Connection: Upgrade\r\n"
//                            + "Upgrade: websocket\r\n"
//                            + "Sec-WebSocket-Accept: "
//                            + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
//                            + "\r\n\r\n").getBytes("UTF-8");
//                    out.write(response, 0, response.length);
//
//                }
//            } finally {
//
//            }
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    public Response(String dataIn) {
        this.dataIn = dataIn;
        //currently only accept get request
        Matcher webSocketMatch = webSocketFrom.matcher(dataIn);
            if (webSocketMatch.find()) {
                //webSocket response
                buildWebSocketHeader(webSocketMatch);
            } else {
                Matcher pathAndTypeMatch = typeAndPathFrom.matcher(dataIn);
                if (pathAndTypeMatch.find()) {
                    buildBody(pathAndTypeMatch.group(1));
                    buildHeader(pathAndTypeMatch.group(2));
                } else {
                    header = badRequest();
                }
            }
    }

    private void buildWebSocketHeader(Matcher webSocketMatch) {
        try {
            byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                    + "Connection: Upgrade\r\n"
                    + "Upgrade: websocket\r\n"
                    + "Sec-WebSocket-Accept: "
                    + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((webSocketMatch.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.UTF_8)))
                    + "\r\n\r\n").getBytes(StandardCharsets.UTF_8);
            this.header = response;
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            header = badRequest();
        }

    }

    
    private void buildHeader(String contentType) {
        header = ("HTTP/1.1 200 Success!\r\n" +
                "dev_env: left blank\r\n" +
                contentTypeLine(contentType) +
                "Content-Length: " + this.body.length + "\r\n\r\n").getBytes(StandardCharsets.UTF_8);
    }

    private String contentTypeLine(String contentType) {
        if(contentType.toLowerCase().contains("css")){
            return "Content-Type: text/css;charset=UTF-8\r\n";
        } else if (contentType.toLowerCase().contains("js")){
            return "Content-Type: text/javascript;charset=UTF-8\r\n";
        } else if (contentType.toLowerCase().contains("html")){
            return "Content-Type: text/html;charset=UTF-8\r\n";
        } else {
            //image 
            return "Content-Type: image/" + contentType.substring(1) + "\r\n";
        }
    }

    private void buildBody(String path) {
            if(path.toLowerCase().endsWith("livewrapper.js")){
                try {
                    this.body = getFileFromResource("liveWrapper.js").readAllBytes();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {

                String localFilePath = App.getDirectory() + path;
                try {
                    this.body = Files.readAllBytes(Paths.get(localFilePath));


                    //if html inject script tag for webSocket
                    if(path.toLowerCase().endsWith("html")){
                     injectLiveLoadTag();
                    }
                } catch (IOException e){
                    e.printStackTrace();
                    this.body = new byte[0];
                }
            }
    }

    public byte[] getLiveLoadTag(){
        return ("<script src=\"liveWrapper.js\"></script>").getBytes(StandardCharsets.UTF_8);
    }

    public void injectLiveLoadTag(){
        ByteBuffer bb = ByteBuffer.wrap(this.body);
        for(int i = this.body.length-1; i >= 0; i--){
            if(this.body[i] == "<".getBytes(StandardCharsets.UTF_8)[0]){
                byte[] upToClosingTag = new byte[i];
                byte[] closingTag = new byte[body.length - (i+1)];
                byte[] liveLoadTag = getLiveLoadTag();

                bb.get(upToClosingTag, 0, upToClosingTag.length);
                bb.get(closingTag, 0, closingTag.length);

                this.body = ByteBuffer.allocate(body.length + liveLoadTag.length).put(upToClosingTag).put(liveLoadTag).put(closingTag).array();
                break;
            }
        }
    }

    public InputStream getFileFromResource(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        if (inputStream == null){
            throw new IllegalArgumentException("file not found: " + fileName);
        } else {
            return inputStream;
        }
    }


    public byte[] getResponse(){
        try(ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream()) {
            byteOutputStream.write(this.header);
            byteOutputStream.write(this.body);
            return byteOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return ("HTTP/1.1 400 Bad Request\r\n").getBytes(StandardCharsets.UTF_8);

        }
    }

    public byte[] badRequest(){
        return ("HTTP/1.1 400 Bad Request\r\n").getBytes(StandardCharsets.UTF_8);
    }

//    public byte[] fileNotFound(){
//        return  ("HTTP/1.1 404 Not Found\r\n" +
//                "Content-Type: text/html\r\n" +
//                "Content-Length: 122\r\n" +
//                "Connection: keep-alive\r\n" +
//                "\r\n\r\n" +
//                "<html>" +
//                "<head><title>404 Not Found</title></head>" +
//                "<body bgcolor='white'>" +
//                "<center><h1>404 Not Found</h1></center>" +
//                "</body>" +
//                "</html>").getBytes(StandardCharsets.UTF_8);
//    }
//
//    private byte[] makeHeader() {
//        String contentLine = "Content-Type: image/" + dataIn + "\n";
//
//        if(dataIn.toLowerCase().contains("html") || dataIn.contains("js") || dataIn.contains("css")){
//            contentLine = contentLine.replace("\n", "; charset=UTF-8\n");
//            contentLine = contentLine.replace("image", "text");
//        }
//
//        return ("HTTP/1.1 200 Success!\n" +
//                "dev_env: left blank\n" +
//                contentLine +
//                "Content-Length: " + this.body.length + "\n\n").getBytes(StandardCharsets.UTF_8);
//    }

//    private String textBodyData() {
//        //get file from path and turn it into a string
//        //if html then adds livewrapper
//
//        StringBuilder dataStringBuilder = new StringBuilder();
//        try (
//                InputStream inputStream = new FileInputStream(new File(path));
//                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))
//        ) {
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

//    private byte[] imageBodyData() {
//
//        try {
//            return Files.readAllBytes(Paths.get(path));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        try(
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        ) {
//            BufferedImage bImage = ImageIO.read(new File(path));
//            ImageIO.write(bImage, this.contentType, bos );
////            body = bos.toByteArray();
//            String text = new String(bos.toByteArray(), StandardCharsets.UTF_8);
//            body = text.getBytes(StandardCharsets.UTF_8);
//            return body;
//            } catch (IOException e) {
//                 e.printStackTrace();
//        }
//                 body = "Content read Error".getBytes(StandardCharsets.UTF_8);
//                 return body;
//    }
    
    
}
//        //set up body and header
//        if(dataIn.toLowerCase().contains("html") || dataIn.contains("css") || dataIn.contains("js")) {
//            //file ( byles[].utf-8 + header + bytes[]
//            if(path.toLowerCase().contains("livewrapper.js")){
//                this.path = "/Users/thomaspetty/.liveload/livewrapper.js";
//            }
//            this.body = textBodyData().getBytes(StandardCharsets.UTF_8);
//        } else {
////             assume image but could be movie or other ( could return a few options as types - only image for now)
//            // || bytes[] + header + bytes[])
//            this.body = imageBodyData();
//        }
//

//        this.header = makeHeader();
