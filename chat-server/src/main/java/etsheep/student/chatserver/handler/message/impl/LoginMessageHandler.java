package etsheep.student.chatserver.handler.message.impl;

import etsheep.student.chat.common.domain.*;
import etsheep.student.chat.common.enumeration.ResponseCode;
import etsheep.student.chat.common.enumeration.ResponseType;
import etsheep.student.chat.common.util.ProtoStuffUtil;
import etsheep.student.chatserver.handler.message.MessageHandler;
import etsheep.student.chatserver.property.PromptMsgProperty;
import etsheep.student.chatserver.user.UserManager;
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

@Component("MessageHandler.login")
public class LoginMessageHandler extends MessageHandler {

    @Autowired
    private UserManager userManager;

    @Override
    public void handle(Message message, Selector server, SelectionKey client, BlockingQueue<Task> queue, AtomicInteger onlineUsers) throws InterruptedException {
        SocketChannel clientChannel = (SocketChannel) client.channel();
        MessageHeader header = message.getHeader();
        String username = header.getSender();
        String password = new String(message.getBody(), PromptMsgProperty.charset);
        try{
            if (userManager.login(clientChannel, username, password)){
                byte[] response = ProtoStuffUtil.serialize(
                        new Response(
                                ResponseHeader.builder()
                                        .type(ResponseType.PROMPT)
                                        .sender(message.getHeader().getSender())
                                        .timestamp(message.getHeader().getTimestamp())
                                        .responseCode(ResponseCode.LOGIN_SUCCESS.getCode())
                                        .build(),
                                String.format(PromptMsgProperty.LOGIN_SUCCESS, onlineUsers.incrementAndGet()).getBytes(PromptMsgProperty.charset)
                        )
                );
                clientChannel.write(ByteBuffer.wrap(response));
                //连续发送信息不可行,必须要暂时中断一下
                //粘包问题
                Thread.sleep(10);
                //登录提示广播
                byte[] loginBroadcast = ProtoStuffUtil.serialize(
                        new Response(
                                ResponseHeader.builder()
                                        .type(ResponseType.NORMAL)
                                        .sender(SYSTEM_SENDER)
                                        .timestamp(message.getHeader().getTimestamp())
                                        .build(),
                                    String.format(PromptMsgProperty.LOGIN_BROADCAST, message.getHeader().getSender()).getBytes(PromptMsgProperty.charset)
                        )
                );
                super.broadcast(loginBroadcast, server);

            }else{
                byte[] response = ProtoStuffUtil.serialize(
                        new Response(
                                ResponseHeader.builder()
                                        .type(ResponseType.PROMPT)
                                        .sender(message.getHeader().getSender())
                                        .responseCode(ResponseCode.LOGIN_FAILURE.getCode())
                                        .timestamp(message.getHeader().getTimestamp())
                                        .build(),
                                    PromptMsgProperty.LOGIN_FAILURE.getBytes(PromptMsgProperty.charset)
                        )
                );
                clientChannel.write(ByteBuffer.wrap(response));
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
