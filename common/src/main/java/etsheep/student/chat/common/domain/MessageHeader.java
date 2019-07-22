package etsheep.student.chat.common.domain;

import etsheep.student.chat.common.enumeration.MessageType;
import lombok.Builder;
import lombok.Data;

/**
 * Created by etsheep on 2019-7-19.
 */
@Data
@Builder
public class MessageHeader {
    private String sender;
    private String receiver;
    private MessageType type;
    private Long timestamp;
}
