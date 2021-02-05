package LiveLoad;

import java.io.IOException;
import java.nio.file.*;

public class DirWatch extends Thread{
    private Path dir;
    private WatchService watchService;
    private WatchKey watchKey;

    public DirWatch(String dir) throws IOException {
        this.dir = Paths.get(dir);

        watchService = FileSystems.getDefault().newWatchService();
        watchKey = this.dir.register(watchService,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE);
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
