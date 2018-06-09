package com.jianhua.naio.aio.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author lijianhua
 */
public class AIOClient {

    public AIOClient(String address, int port) throws ExecutionException, InterruptedException, IOException {
        AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open();
        Future<Void> connect = socketChannel.connect(new InetSocketAddress(address, port));
        connect.get();
        System.out.println("连接成功");

        Attachment attachment = new Attachment();
        attachment.setClient(socketChannel);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put("Hello".getBytes(Charset.defaultCharset()));
        attachment.setBuffer(buffer);
        attachment.setMode(Attachment.MODE.WRITE);
        socketChannel.write(attachment.getBuffer(), attachment, new CompletionHandler<Integer, Attachment>() {
            @Override
            public void completed(Integer result, Attachment attachment) {
                if(attachment.getMode() == Attachment.MODE.WRITE){
                    attachment.setMode(Attachment.MODE.READ);
                    attachment.getBuffer().clear();
                    attachment.getClient().read(attachment.getBuffer(), attachment, this);
                }else{
                    ByteBuffer buffer1 = attachment.getBuffer();
                    buffer1.flip();
                    byte[] readData = new byte[buffer1.limit()];
                    buffer1.get(readData);
                    System.out.println("收到来自服务器的数据：" + new String(readData));

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    buffer1.clear();
                    attachment.setMode(Attachment.MODE.WRITE);
                    buffer1.put("Hello".getBytes(Charset.defaultCharset()));
                    buffer1.flip();
                    attachment.getClient().write(buffer1, attachment, this);
                }
            }

            @Override
            public void failed(Throwable exc, Attachment attachment) {
                System.out.println("发送数据失败");
            }
        });

        Thread.currentThread().join();
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        new AIOClient("127.0.0.1", 8080);
    }

}