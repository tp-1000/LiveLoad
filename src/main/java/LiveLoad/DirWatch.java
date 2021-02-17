package LiveLoad;

import com.sun.nio.file.SensitivityWatchEventModifier;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;


//Ugly implementation on mac - falls back to polling :(
//Create class to watch directory for changes
//it also closes websocket

public class DirWatch implements Runnable {
    private Path dir;
    private WatchService watchService;
    private WatchKey watchKey;
    private static AtomicBoolean isWatchingForSocket = new AtomicBoolean(false);


    public DirWatch(String path) throws IOException {
        isWatchingForSocket.set(false);

        this.dir = Paths.get(path);

        watchService = FileSystems.getDefault().newWatchService();
        watchKey = this.dir.register(watchService,
                new WatchEvent.Kind[]{
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY},
                SensitivityWatchEventModifier.HIGH);
    }


    public void run() {
        isWatchingForSocket.set(true);

        while (isWatchingForSocket.get()) {
            for (WatchEvent<?> event : watchKey.pollEvents()) {
                if(event.count() > 0){
                    System.out.println(event.context().toString());
                    if(!event.context().toString().endsWith("swp")){
                        try {
                            isWatchingForSocket.set(false);
                            watchService.close();
                            watchKey.cancel();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        try {
            watchService.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        watchKey.cancel();
    }
}
