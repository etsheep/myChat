package etsheep.student.chatserver.handler.task;

import etsheep.student.chat.common.domain.Response;
import etsheep.student.chat.common.domain.Task;
import etsheep.student.chat.common.util.ProtoStuffUtil;
import etsheep.student.chatserver.exception.TaskException;
import etsheep.student.chatserver.http.HttpConnectionManager;
import etsheep.student.chatserver.task.TaskManagerThread;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by etsheep on 2019-7-20.
 */
public abstract class BaseTaskHandler implements Runnable {

    protected Task info;
    protected HttpConnectionManager manager;

    protected abstract Response process() throws IOException, InterruptedException;

    protected abstract void init(TaskManagerThread parentThread);

    @Override
    public void run() {
        try {
            info.getReceiver().write(ByteBuffer.wrap(ProtoStuffUtil.serialize(process())));
        } catch (IOException e) {
            e.printStackTrace();
            throw new TaskException(info);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new TaskException(info);
        }
    }

    public void init(Task info, HttpConnectionManager manager, TaskManagerThread parentThread){
        this.info = info;
        this.manager = manager;
        init(parentThread);
    }
}
