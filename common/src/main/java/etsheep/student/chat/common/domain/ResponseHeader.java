package etsheep.student.chat.common.domain;

import etsheep.student.chat.common.enumeration.ResponseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by etsheep on 2019-7-19.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseHeader {
    private String sender;
    private ResponseType type;
    private Integer responseCode;
    private Long timestamp;
}
