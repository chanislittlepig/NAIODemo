package com.jianhua.naio.nio.demo.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author lijianhua
 */
public class NIOClient {

    private Selector selector;


    public NIOClient(String address, int port) throws IOException {
        selector = Selector.open();
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.socket().setTcpNoDelay(true);
        channel.connect(new InetSocketAddress(address, port));
        channel.register(selector, SelectionKey.OP_CONNECT);

    }

    public void start() throws IOException {
        System.out.println("等待连入服务器");
        while(true){
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectionKeys.iterator();
            while(it.hasNext()){
                SelectionKey key = it.next();
                it.remove();
                if(key.isConnectable()){
                    SocketChannel channel = (SocketChannel) key.channel();
                    if(channel.finishConnect()){
                        System.out.println("连接服务器成功");
                        key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        buffer.put("HelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHello".getBytes("UTF-8"));
                        buffer.flip();
                        int write = channel.write(buffer);

                        if(write == 0){
                            key.attach(buffer);
                            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                            selector.wakeup();
                        }else{
                            System.out.println("服务器繁忙，稍后再试");
                        }

                    }else{
                        System.out.println("connection fail");
                        key.cancel();
                        channel.close();
                    }
                }else if(key.isReadable()){
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buf = ByteBuffer.allocate(1024);
                    int read = channel.read(buf);
                    if(read > 0){
                        buf.flip();
                        System.out.println("收到服务器的回复:"+new String(buf.array(),"UTF-8"));
                    }else if(read < 0){
                        System.out.println("连接服务器失败");
                        key.cancel();
                        channel.close();
                    }
                }else if(key.isWritable()){
                    System.out.println("服务器写入就绪，开始发送请求");
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    if(buffer != null && buffer.hasRemaining()){
                        channel.write(buffer);
                    }
                    key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                }
            }
        }
    }


    public static void main(String[] args) throws IOException {
      new NIOClient("localhost", 8080).start();
    }


}
