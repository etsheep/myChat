package etsheep.student.chat.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by etsheep on 2019-7-19.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    private ResponseHeader header;
    private byte[] body;
}
