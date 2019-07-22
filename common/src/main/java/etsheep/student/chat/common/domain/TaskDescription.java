package etsheep.student.chat.common.domain;

import etsheep.student.chat.common.enumeration.TaskType;
import lombok.Data;

/**
 * Created by etsheep on 2019-7-19.
 */
@Data
public class TaskDescription {
    private TaskType type;
    private String desc;
}
