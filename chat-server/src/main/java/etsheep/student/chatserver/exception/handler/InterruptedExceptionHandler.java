package etsheep.student.chatserver.exception.handler;

import etsheep.student.chat.common.domain.Message;
import etsheep.student.chat.common.domain.Response;
import etsheep.student.chat.common.domain.ResponseHeader;
import etsheep.student.chat.common.enumeration.ResponseType;
import etsheep.student.chat.common.util.ProtoStuffUtil;
import etsheep.student.chatserver.property.PromptMsgProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by etsheep on 2019-7-20.
 */

@Component("interruptedExceptionHandler")
public class InterruptedExceptionHandler {

    public void handle(SocketChannel channel, Message message){
        try{
            byte[] response = ProtoStuffUtil.serialize(
                    new Response(
                            ResponseHeader.builder().type(ResponseType.PROMPT)
                                    .sender(message.getHeader().getSender())
                                    .timestamp(message.getHeader().getTimestamp()).build(),
                            PromptMsgProperty.SERVER_ERROR.getBytes(PromptMsgProperty.charset)
                    )
            );
            channel.write(ByteBuffer.wrap(response));
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
