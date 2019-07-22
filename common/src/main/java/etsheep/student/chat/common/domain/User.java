package etsheep.student.chat.common.domain;

import lombok.Builder;
import lombok.Data;

import java.nio.channels.SocketChannel;

/**
 * Created by etsheep on 2019-7-19.
 */
@Data
@Builder
public class User {
    private String username;
    private String password;
    private SocketChannel channel;
}
