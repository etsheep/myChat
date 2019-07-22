package etsheep.student.chat.client;

import etsheep.student.chat.common.domain.Message;
import etsheep.student.chat.common.domain.Response;
import etsheep.student.chat.common.domain.ResponseHeader;
import etsheep.student.chat.common.enumeration.ResponseCode;
import etsheep.student.chat.common.util.ProtoStuffUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * Created by etsheep on 2019-7-22.
 */
public class ChatClient {

    public static final int DEFAULT_BUFFER_SIZE = 1024;
    private Selector clientSelector;
    private SocketChannel clientChannel;
    private ByteBuffer buffer;

    private Charset charset = StandardCharsets.UTF_8;

    public void initNetWork(){
        try {
            clientSelector = Selector.open();
            clientChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9000));
            clientChannel.configureBlocking(false);
            clientChannel.register(clientSelector, SelectionKey.OP_READ);
            buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class ReceiverHandler implements Runnable {

        private boolean connected = true;

        public void shutdown(){
            connected = false;
        }

        public void run() {
            try {
                while (true) {
                    int size = 0;
                    clientSelector.select();
                    for (Iterator<SelectionKey> it = clientSelector.selectedKeys().iterator(); it.hasNext();){
                        SelectionKey selectionKey = it.next();
                        it.remove();
                        if (selectionKey.isReadable()){
                            try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
                                SocketChannel channel = (SocketChannel) selectionKey.channel();
                                while ((size = channel.read(buffer)) > 0){
                                    buffer.flip();
                                    baos.write(buffer.array(), 0, size);
                                    buffer.clear();
                                }
                                byte[] bytes = baos.toByteArray();
                                Response response = ProtoStuffUtil.deserialize(bytes, Response.class);
                                handleResponse(response);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleResponse(Response response) {
            System.out.println(response);
            ResponseHeader header = response.getHeader();
            switch (header.getType()){
                case PROMPT:
                    if (header.getResponseCode() != null) {
                        ResponseCode code = ResponseCode.fromCode(header.getResponseCode());
                        if (code == ResponseCode.LOGIN_SUCCESS) {
//                            isLogin = true;
                            System.out.println("登录成功");
                        } else if (code == ResponseCode.LOGOUT_SUCCESS) {
                            System.out.println("下线成功");
                            break;
                        }
                    }
                    String info = new String(response.getBody(), charset);
                case NORMAL:
                    break;
                case FILE:
                    break;
            }
        }
    }
}
