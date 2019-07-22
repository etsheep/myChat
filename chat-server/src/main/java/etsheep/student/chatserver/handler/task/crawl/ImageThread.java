package etsheep.student.chatserver.handler.task.crawl;

import etsheep.student.chatserver.http.HttpConnectionManager;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Created by etsheep on 2019-7-21.
 */
@Slf4j
public class ImageThread implements Callable<byte[]> {

    private String url;
    private HttpConnectionManager manager;
    public ImageThread(String url, HttpConnectionManager manager){
        this.url = url;
        this.manager = manager;
    }

    @Override
    public byte[] call() throws Exception {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            if (!manager.copyStream(url, baos)){
                throw new IOException();
            }
            log.info("下载图片成功");
            byte[] bytes = baos.toByteArray();
            return bytes;
        }
    }
}
