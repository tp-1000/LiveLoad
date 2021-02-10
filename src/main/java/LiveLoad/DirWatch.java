package LiveLoad;

import com.sun.nio.file.SensitivityWatchEventModifier;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.*;


//Ugly implementation on mac - falls back to polling :(
public class DirWatch extends Thread{
    private Path dir;
    private WatchService watchService;
    private WatchKey watchKey;
    private Socket socket;
    private Boolean isRunning = false;

    public DirWatch(Socket socket) throws IOException {
        this.dir = Paths.get(App.getDirectory());
        this.socket = socket;

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
        isRunning = true;
        while(isRunning){
            //may want to terminate if client disappears
            for (WatchEvent<?> event : watchKey.pollEvents()){
                try {
                    isRunning = false;
                    watchService.close();
                    watchKey.cancel();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
