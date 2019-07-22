package etsheep.student.chatserver.exception.handler;

import etsheep.student.chat.common.domain.Message;
import etsheep.student.chat.common.domain.Response;
import etsheep.student.chat.common.domain.ResponseHeader;
import etsheep.student.chat.common.domain.Task;
import etsheep.student.chat.common.enumeration.ResponseType;
import etsheep.student.chat.common.util.ProtoStuffUtil;
import etsheep.student.chatserver.exception.TaskException;
import etsheep.student.chatserver.property.PromptMsgProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by etsheep on 2019-7-21.
 * UncaughtExceptionHandler异常处理器可以处理ExecutorService通过execute方法提交的线程中抛出的RuntimeException
 */

@Component("taskExceptionHandler")
@Slf4j
public class TaskExceptionHandler implements Thread.UncaughtExceptionHandler{
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try{
            if (e instanceof TaskException){
                TaskException taskException = (TaskException) e;
                Task task = taskException.getInfo();
                Message message = task.getMessage();
                byte[] response = ProtoStuffUtil.serialize(
                        new Response(
                                ResponseHeader.builder()
                                        .type(ResponseType.PROMPT)
                                        .sender(message.getHeader().getSender())
                                        .timestamp(message.getHeader().getTimestamp()).build(),
                                PromptMsgProperty.SERVER_ERROR.getBytes(PromptMsgProperty.charset)
                        )
                );
//            log.info("返回任务执行失败信息");
                task.getReceiver().write(ByteBuffer.wrap(response));
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
