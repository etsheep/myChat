package etsheep.student.chat.common.domain;

import lombok.Data;

/**
 * Created by etsheep on 2019-7-19.
 */
@Data
public class Message {
    private MessageHeader header;
    private byte[] body;
}
