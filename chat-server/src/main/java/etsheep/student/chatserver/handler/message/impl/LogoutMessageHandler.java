package etsheep.student.chatserver.handler.message.impl;

import etsheep.student.chat.common.domain.Message;
import etsheep.student.chat.common.domain.Response;
import etsheep.student.chat.common.domain.ResponseHeader;
import etsheep.student.chat.common.domain.Task;
import etsheep.student.chat.common.enumeration.ResponseCode;
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

@Component("MessageHandler.logout")
@Slf4j
public class LogoutMessageHandler extends MessageHandler {


    @Autowired
    private UserManager userManager;

    @Override
    public void handle(Message message, Selector server, SelectionKey client, BlockingQueue<Task> queue, AtomicInteger onlineUsers) throws InterruptedException {
        try{
            SocketChannel clientChannel = (SocketChannel) client.channel();
            userManager.logout(clientChannel);
            byte[] response = ProtoStuffUtil.serialize(
                    new Response(
                            ResponseHeader.builder()
                                    .type(ResponseType.PROMPT)
                                    .responseCode(ResponseCode.LOGOUT_SUCCESS.getCode())
                                    .sender(message.getHeader().getSender())
                                    .timestamp(message.getHeader().getTimestamp())
                                    .build(),
                            PromptMsgProperty.LOGOUT_SUCCESS.getBytes(PromptMsgProperty.charset)
                    )
            );
            clientChannel.write(ByteBuffer.wrap(response));
            onlineUsers.decrementAndGet();
            //下线广播
            byte[] logoutBroadcast = ProtoStuffUtil.serialize(
                    new Response(
                            ResponseHeader.builder()
                                    .type(ResponseType.PROMPT)
                                    .sender(SYSTEM_SENDER)
                                    .timestamp(message.getHeader().getTimestamp())
                                    .build(),
                            String.format(PromptMsgProperty.LOGOUT_BROADCAST, message.getHeader().getSender()).getBytes(PromptMsgProperty.charset)
                    )
            );
            super.broadcast(logoutBroadcast, server);
            log.info("客户端退出");
            //必须要cancel，否则无法从keys从去除该客户端
            client.cancel();
            clientChannel.close();
            clientChannel.socket().close();
        }catch (IOException e) {
            e.printStackTrace();
        }

    }
}
