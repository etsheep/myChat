package etsheep.student.chat.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Created by etsheep on 2019-7-19.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    private String url;
    private Map<String,String> params;
}
