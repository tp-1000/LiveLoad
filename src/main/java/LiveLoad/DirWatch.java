package LiveLoad;

import com.sun.nio.file.SensitivityWatchEventModifier;

import java.io.IOException;
import java.nio.file.*;


//Ugly implementation on mac - falls back to polling :(
public class DirWatch extends Thread{
    private Path dir;
    private WatchService watchService;
    private WatchKey watchKey;

    public DirWatch(String dir) throws IOException {
        this.dir = Paths.get(dir);

        watchService = FileSystems.getDefault().newWatchService();
        watchKey = this.dir.register(watchService,
                new WatchEvent.Kind[]{
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY},
                SensitivityWatchEventModifier.HIGH);
    }

    @Override
    public void run() {
        while(true){
            for (WatchEvent<?> event : watchKey.pollEvents()){
                System.out.println(event.kind());
            }
        }
    }
}
