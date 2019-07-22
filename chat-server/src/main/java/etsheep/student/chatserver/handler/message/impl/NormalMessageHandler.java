package etsheep.student.chatserver.handler.message.impl;

import etsheep.student.chat.common.domain.*;
import etsheep.student.chat.common.enumeration.ResponseType;
import etsheep.student.chat.common.util.ProtoStuffUtil;
import etsheep.student.chatserver.handler.message.MessageHandler;
import etsheep.student.chatserver.property.PromptMsgProperty;
import etsheep.student.chatserver.user.UserManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by etsheep on 2019-7-21.
 */

@Component("MessageHandler.normal")
@Slf4j
public class NormalMessageHandler extends MessageHandler {

    @Autowired
    private UserManager userManager;

    @Override
    public void handle(Message message, Selector server, SelectionKey client, BlockingQueue<Task> queue, AtomicInteger onlineUsers) throws InterruptedException {
        try{
            SocketChannel clientChannel = (SocketChannel) client.channel();
            MessageHeader header = message.getHeader();
            SocketChannel receiverChannel = userManager.getUserChannel(header.getReceiver());
            if (receiverChannel == null){
                //接收者下线
                byte[] response = ProtoStuffUtil.serialize(
                        new Response(
                                ResponseHeader.builder()
                                        .type(ResponseType.PROMPT)
                                        .sender(header.getSender())
                                        .timestamp(header.getTimestamp()).build(),
                                PromptMsgProperty.RECEIVER_LOGGED_OFF.getBytes(PromptMsgProperty.charset)
                        )
                );
                clientChannel.write(ByteBuffer.wrap(response));
            }else{
                byte[] response = ProtoStuffUtil.serialize(
                        new Response(
                                ResponseHeader.builder()
                                        .type(ResponseType.NORMAL)
                                        .sender(header.getSender())
                                        .timestamp(header.getTimestamp())
                                        .build(),
                                    message.getBody()
                        )
                );
                receiverChannel.write(ByteBuffer.wrap(response));
                log.info("已转发给", receiverChannel);
                //也给自己发送一份
                clientChannel.write(ByteBuffer.wrap(response));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
