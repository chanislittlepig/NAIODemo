package com.jianhua.naio.aio.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * @author lijianhua
 */
public class AIOServer {

   public AIOServer(int port) throws IOException {
       AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(port));
        Attachment attachment = new Attachment();
        attachment.setServer(serverSocketChannel);
       serverSocketChannel.accept(attachment, new CompletionHandler<AsynchronousSocketChannel, Attachment>() {
           @Override
           public void completed(AsynchronousSocketChannel socketChannel, Attachment attachment) {
               serverSocketChannel.accept(attachment, this);

               Attachment att = new Attachment();
               att.setServer(serverSocketChannel);
               att.setClient(socketChannel);
               att.setMode(Attachment.MODE.READ);
               att.setBuffer(ByteBuffer.allocate(1024));
               socketChannel.read(att.getBuffer(), att, new IOHandler());
           }

           @Override
           public void failed(Throwable exc, Attachment attachment) {
               System.out.println("connect fail");
           }
       });

       try {
           Thread.currentThread().join();
       } catch (InterruptedException e) {
           e.printStackTrace();
       }
   }


    private class IOHandler implements CompletionHandler<Integer, Attachment> {

        @Override
        public void completed(Integer result, Attachment attachment) {
            if(attachment.getMode() == Attachment.MODE.READ) {
                ByteBuffer buffer = attachment.getBuffer();
                buffer.flip();
                byte[] readData = new byte[buffer.limit()];
                buffer.get(readData);
                System.out.println("收到来自客户端的数据：" + new String(readData));

                buffer.clear();
                buffer.put(new Date().toString().getBytes(Charset.defaultCharset()));
                attachment.setMode(Attachment.MODE.WRITE);
                buffer.flip();
                attachment.getClient().write(buffer, attachment, this);

            }else{
                ByteBuffer buffer = attachment.getBuffer();
                buffer.clear();
                attachment.setMode(Attachment.MODE.READ);
                attachment.getClient().read(attachment.getBuffer(), attachment, this);
            }

        }

        @Override
        public void failed(Throwable exc, Attachment attachment) {
            System.out.println("read fail");
        }
    }

    public static void main(String[] args) throws IOException {
        new AIOServer(8080);
    }
}
