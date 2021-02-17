package LiveLoad;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Response {

    private byte[] body = new byte[0];
    private byte[] header;
    private String dataIn;

    Pattern typeAndPathFrom = Pattern.compile("GET /([^.]*([^\\s]*))");
    Pattern webSocketFrom = Pattern.compile("Sec-WebSocket-Key: (.*)");
    Pattern getFrom = Pattern.compile("^GET");


    public Response(int statusCode) {
        //TODO make constructor to error status codes
        //makes new  request object
    }

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
        } catch (NoSuchAlgorithmException e) {
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
        if (contentType.toLowerCase().contains("css")) {
            return "Content-Type: text/css;charset=UTF-8\r\n";
        } else if (contentType.toLowerCase().contains("js")) {
            return "Content-Type: text/javascript;charset=UTF-8\r\n";
        } else if (contentType.toLowerCase().contains("html")) {
            return "Content-Type: text/html;charset=UTF-8\r\n";
        } else {
            //image 
            return "Content-Type: image/" + contentType.substring(1) + "\r\n";
        }
    }

    private void buildBody(String path) {
        if (path.toLowerCase().endsWith("livewrapper.js")) {
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
                if (path.toLowerCase().endsWith("html")) {
                    injectLiveLoadTag();
                }
            } catch (IOException e) {
                e.printStackTrace();
                this.body = new byte[0];
            }
        }
    }

    public byte[] getLiveLoadTag() {
        return ("<script src=\"liveWrapper.js\"></script>").getBytes(StandardCharsets.UTF_8);
    }

    public void injectLiveLoadTag() {
        ByteBuffer bb = ByteBuffer.wrap(this.body);
        for (int i = this.body.length - 1; i >= 0; i--) {
            if (this.body[i] == "<".getBytes(StandardCharsets.UTF_8)[0]) {
                byte[] upToClosingTag = new byte[i];
                byte[] closingTag = new byte[body.length - (i + 1)];
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
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found: " + fileName);
        } else {
            return inputStream;
        }
    }


    public byte[] getResponse() {
        try (ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream()) {
            byteOutputStream.write(this.header);
            byteOutputStream.write(this.body);
            return byteOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return ("HTTP/1.1 400 Bad Request\r\n").getBytes(StandardCharsets.UTF_8);

        }
    }

    public byte[] badRequest() {
        return ("HTTP/1.1 400 Bad Request\r\n").getBytes(StandardCharsets.UTF_8);
    }
}