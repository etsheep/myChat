package etsheep.student.chatserver.handler.task.file;

import etsheep.student.chat.common.domain.*;
import etsheep.student.chat.common.enumeration.ResponseType;
import etsheep.student.chatserver.exception.TaskException;
import etsheep.student.chatserver.handler.task.BaseTaskHandler;
import etsheep.student.chatserver.task.TaskManagerThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by etsheep on 2019-7-21.
 */

@Component("BaseTaskHandler.file")
@Scope("prototype")
@Slf4j
public class FileTaskHandler extends BaseTaskHandler {


    @Override
    protected Response process() throws IOException {
        MessageHeader header = info.getMessage().getHeader();
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            System.out.println(info);
            if (!manager.copyStream(info.getDesc(), baos)){
                throw new TaskException(info);
            }
//        log.info("下载图片成功");
            byte[] bytes = baos.toByteArray();
            return new Response(ResponseHeader.builder()
                    .type(ResponseType.FILE)
                    .sender(header.getSender())
                    .timestamp(header.getTimestamp())
                    .build(),
                bytes);
        }
    }

    @Override
    protected void init(TaskManagerThread parentThread) {
        //不需要其他数据
    }
}
