package com.jianhua.naio.nio.demo.moniter;

import java.nio.file.StandardWatchEventKinds;
import java.util.Observable;
import java.util.Observer;

/**
 * @author lijianhua
 */
public abstract class FileChangeListener implements Observer {

    public abstract void onCreate(String filename) throws Exception;

    public abstract void onDelete(String filename) throws Exception;

    public abstract void onModified(String filename) throws Exception;

    public abstract void onException(String filename) throws Exception;

    @Override
    public void update(Observable o, Object event) {
        FileEvent fileEvent = (FileEvent) event;
        String eventType = fileEvent.getKind();
        try {
            if (StandardWatchEventKinds.ENTRY_CREATE.name().equals(eventType)) {
                onCreate(fileEvent.getFilename());
            }else if(StandardWatchEventKinds.ENTRY_DELETE.name().equals(eventType)){
                onDelete(fileEvent.getFilename());
            }else if(StandardWatchEventKinds.ENTRY_MODIFY.name().equals(eventType)){
                onModified(fileEvent.getFilename());
            }else if(StandardWatchEventKinds.OVERFLOW.name().equals(eventType)){
                onException(fileEvent.getFilename());
            }
        }catch (Exception e){
        }
    }
}