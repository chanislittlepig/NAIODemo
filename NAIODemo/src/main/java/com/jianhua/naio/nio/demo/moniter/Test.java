package com.jianhua.naio.nio.demo.moniter;

import java.io.IOException;

/**
 * @author lijianhua
 */
public class Test {

    public static void main(String[] args) throws IOException {
        FileWatcher watcher = new FileWatcher(System.getProperty("user.dir"), true);
        watcher.register(new FileChangeListener() {
            @Override
            public void onCreate(String filename) throws Exception {
                System.out.println("新增文件：" + filename);
            }

            @Override
            public void onDelete(String filename) throws Exception {
                System.out.println("删除文件：" + filename);
            }

            @Override
            public void onModified(String filename) throws Exception {
                System.out.println("修改文件：" + filename);
            }

            @Override
            public void onException(String filename) throws Exception {
                System.out.println("程序异常");
                watcher.stop();
            }
        });
        watcher.start();
    }

}