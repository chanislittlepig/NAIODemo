package com.jianhua.naio.nio.demo.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author lijianhua
 */
public class NIOServer {

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

    private  ExecutorService executor = null;

    public NIOServer(int port) throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        executor = Executors.newFixedThreadPool(10);
    }

    public void start() throws IOException {
        while(true){
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                iterator.remove();
                SocketChannel channel = null;
                try {
                    if (key.isAcceptable()) {
                        System.out.println("客户端进入");
                        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                         channel = serverChannel.accept();
                        channel.configureBlocking(false);
                        channel.socket().setTcpNoDelay(true);
                        channel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        channel = (SocketChannel) key.channel();
                        ByteBuffer byteBuf = ByteBuffer.allocate(1024);
                        int read = channel.read(byteBuf);
                        if (read > 0) {
                            byteBuf.flip();
                            byte[] bs = new byte[byteBuf.limit()];
                            byteBuf.get(bs);
                            System.out.println("收到客户端的请求:" + new String(bs));

                            ByteBuffer writeBuf = ByteBuffer.allocate(1024);
                            writeBuf.put(new Date().toString().getBytes(Charset.defaultCharset()));
                            writeBuf.flip();
                            int write = channel.write(writeBuf);
                            if (write == 0) {
                                key.attach(writeBuf);
                                key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                                selector.wakeup();
                            }
                        } else if (read < 0) {
                            System.out.println("客户端断开连接");
                            key.cancel();
                            channel.close();
                        } else {
                        }
                    } else if (key.isWritable()) {
                         channel = (SocketChannel) key.channel();
                        ByteBuffer toWriteBuf = (ByteBuffer) key.attachment();
                        if (toWriteBuf != null && toWriteBuf.hasRemaining()) {
                            channel.write(toWriteBuf);
                        }
                        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                    }
                }catch (Exception e){
                    key.cancel();
                    if(channel!=null){
                        channel.close();
                    }
                }
            }
        }


    }

    public static void main(String[] args) throws IOException {
        new NIOServer(8080).start();
    }

}
