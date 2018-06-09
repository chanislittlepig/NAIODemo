package com.jianhua.naio.nio.demo.moniter;


import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 监控文件增删改的变化
 *
 * @author lijianhua
 */
public class FileWatcher extends Observable {

    private String toWatchFile;

    private boolean cascade;

    private WatchService watchService;

    private ExecutorService executorService;

    public FileWatcher(String toWatchFile) throws IOException {
        this(toWatchFile, false);
    }

    public FileWatcher(String toWatchFile, boolean cascade) throws IOException {
        this.toWatchFile = toWatchFile;
        this.cascade = cascade;
        initWatchCore();
    }

    private void initWatchCore() throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        executorService = Executors.newSingleThreadExecutor();
        Path toWatchPath = Paths.get(toWatchFile);
        toWatchPath.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.OVERFLOW);
        if(cascade){
            Files.walkFileTree(toWatchPath, new SimpleFileVisitor<Path>(){

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    dir.register(watchService,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_DELETE,
                            StandardWatchEventKinds.ENTRY_MODIFY,
                            StandardWatchEventKinds.OVERFLOW);
                    return FileVisitResult.CONTINUE;
                }
            });
        }

    }

    public void register(FileChangeListener... fileChangeListener){
        if(fileChangeListener == null){
            throw new IllegalStateException();
        }
        for (FileChangeListener changeListener : fileChangeListener) {
            addObserver(changeListener);
        }

    }

    public void start(){
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                processEvents();
            }

        });
    }



    private void processEvents() {
        while(true){
            WatchKey signal = null;
            try {
                signal= watchService.take();
            } catch (InterruptedException e) {
                return;
            }
            List<WatchEvent<?>> watchEvents = signal.pollEvents();
            for (WatchEvent<?> event : watchEvents) {
                setChanged();
                Path path = (Path) event.context();
                notifyFileListener(new FileEvent(path.getFileName().toString(), event.kind().name()));
            }

            signal.reset();
        }
    }

    private void notifyFileListener(FileEvent fileEvent) {
        notifyObservers(fileEvent);
    }

    public void stop() throws IOException {
        executorService.shutdownNow();
        watchService.close();
    }


}