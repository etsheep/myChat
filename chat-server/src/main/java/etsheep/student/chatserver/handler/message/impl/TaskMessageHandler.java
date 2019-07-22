package etsheep.student.chatserver.handler.message.impl;

import etsheep.student.chat.common.domain.Message;
import etsheep.student.chat.common.domain.Task;
import etsheep.student.chat.common.domain.TaskDescription;
import etsheep.student.chat.common.util.ProtoStuffUtil;
import etsheep.student.chatserver.handler.message.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by etsheep on 2019-7-21.
 * 生产者
 * 注意所有的InterruptedException，要么抛给上层，要么自己处理
 */

@Component("MessageHandler.task")
@Slf4j
public class TaskMessageHandler extends MessageHandler {

    @Override
    public void handle(Message message, Selector server, SelectionKey client, BlockingQueue<Task> queue, AtomicInteger onlineUsers) throws InterruptedException {
        TaskDescription taskDescription = ProtoStuffUtil.deserialize(message.getBody(), TaskDescription.class);
        Task task = new Task((SocketChannel) client.channel(), taskDescription.getType(), taskDescription.getDesc(), message);
        try{
            queue.put(task);
            log.info("{}已放入阻塞队列",task.getReceiver().getRemoteAddress());
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
