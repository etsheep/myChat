package etsheep.student.chatserver.handler.message.impl;

import etsheep.student.chat.common.domain.Message;
import etsheep.student.chat.common.domain.Response;
import etsheep.student.chat.common.domain.ResponseHeader;
import etsheep.student.chat.common.domain.Task;
import etsheep.student.chat.common.enumeration.ResponseType;
import etsheep.student.chat.common.util.ProtoStuffUtil;
import etsheep.student.chatserver.handler.message.MessageHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by etsheep on 2019-7-21.
 */

@Component("MessageHandler.broadcast")
public class BroadcastMessageHandler extends MessageHandler {
    @Override
    public void handle(Message message, Selector server, SelectionKey client, BlockingQueue<Task> queue, AtomicInteger onlineUsers) throws InterruptedException {
        try {
            byte[] response = ProtoStuffUtil.serialize(
                    new Response(
                            ResponseHeader.builder()
                                    .type(ResponseType.NORMAL)
                                    .sender(message.getHeader().getSender())
                                    .timestamp(message.getHeader().getTimestamp())
                                    .build(),
                            message.getBody()
                    )
            );
            super.broadcast(response, server);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
