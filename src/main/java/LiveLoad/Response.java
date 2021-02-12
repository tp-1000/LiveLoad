package LiveLoad;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Response {

    private byte[] body;
    private byte[] header;
    private String contentType;
    private String path;




    public Response(String contentType, String path) {
        this.path = path;


        this.contentType = contentType;


        //set up body and header
        if(contentType.toLowerCase().contains("html") || contentType.contains("css") || contentType.contains("js")) {
            //file ( byles[].utf-8 + header + bytes[]
            if(path.toLowerCase().contains("livewrapper.js")){
                this.path = "/Users/thomaspetty/.liveload/livewrapper.js";
            }
            this.body = textBodyData().getBytes(StandardCharsets.UTF_8);
        } else {
//             assume image but could be movie or other ( could return a few options as types - only image for now)
            // || bytes[] + header + bytes[])
            this.body = imageBodyData();
        }

        this.header = makeHeader();

    }

    private byte[] makeHeader() {
        String contentLine = "Content-Type: image/" + contentType + "\n";

        if(contentType.toLowerCase().contains("html") || contentType.contains("js") || contentType.contains("css")){
            contentLine = contentLine.replace("\n", "; charset=UTF-8\n");
            contentLine = contentLine.replace("image", "text");
        }

        return ("HTTP/1.1 200 Success!\n" +
                "dev_env: left blank\n" +
                contentLine +
                "Content-Length: " + this.body.length + "\n\n").getBytes(StandardCharsets.UTF_8);
    }



    private String textBodyData() {
        //get file from path and turn it into a string
        //if html then adds livewrapper

        StringBuilder dataStringBuilder = new StringBuilder();
        try (
                InputStream inputStream = new FileInputStream(new File(path));
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))
        ) {
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

    private byte[] imageBodyData() {

        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                 body = "Content read Error".getBytes(StandardCharsets.UTF_8);
                 return body;
    }

    public byte[] getResponse(){
        try(ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream()) {
            byteOutputStream.write(this.header);
            byteOutputStream.write(this.body);
            return byteOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ("HTTP/1.1 400 Bad Request\r\n").getBytes(StandardCharsets.UTF_8);
    }


}
