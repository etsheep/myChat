package etsheep.student.chat.common.domain;

import etsheep.student.chat.common.enumeration.TaskType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.channels.SocketChannel;

/**
 * Created by etsheep on 2019-7-19.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Task {

    private SocketChannel receiver;
    private TaskType type;
    private String desc;
    private Message message;
}
