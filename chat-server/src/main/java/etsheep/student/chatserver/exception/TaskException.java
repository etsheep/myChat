package etsheep.student.chatserver.exception;

import etsheep.student.chat.common.domain.Task;
import lombok.Data;

/**
 * Created by etsheep on 2019-7-20.
 */

@Data
public class TaskException extends RuntimeException{

    private Task info;
    public TaskException(Task info){
        super(info.getDesc() + "任务执行失败");
        this.info = info;
    }
}
