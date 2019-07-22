package etsheep.student.chatserver;

import etsheep.student.chat.common.domain.Message;
import etsheep.student.chat.common.domain.Task;
import etsheep.student.chat.common.util.ProtoStuffUtil;
import etsheep.student.chatserver.exception.handler.InterruptedExceptionHandler;
import etsheep.student.chatserver.handler.message.MessageHandler;
import etsheep.student.chatserver.task.TaskManagerThread;
import etsheep.student.chatserver.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by etsheep on 2019-7-19.
 */

@Slf4j
public class ChatServer {
    public static final int DEFAULT_BUFFER_SIZE = 1024;
    public static final int PORT = 9000;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    private ExecutorService readPool;

    private BlockingQueue<Task> downloadTaskQueue;

    private ListenerThread listenerThread;
    private AtomicInteger onlineUsers;

    private InterruptedExceptionHandler interruptedExceptionHandler;

    private TaskManagerThread taskManagerThread;

    public ChatServer(){
        initServer();
    }

    private void initServer() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            //切换为非阻塞模式
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            //获得选择器
            selector = Selector.open();
            //将channel注册到selector上
            //第二个参数是选择键，用于说明selector监控channel的状态
            //可能的取值：SelectionKey.OP_READ OP_WRITE OP_CONNECT OP_ACCEPT
            //监控的是channel的接收状态
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            int coreSize = Runtime.getRuntime().availableProcessors() << 1;
            readPool = new ThreadPoolExecutor(coreSize, coreSize << 1, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(200), new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName(UUID.randomUUID().toString());
                    return t;
                }
            }, new CallerRunsPolicy());

            downloadTaskQueue = new ArrayBlockingQueue<>(20);
            taskManagerThread = new TaskManagerThread(downloadTaskQueue);
            taskManagerThread.setUncaughtExceptionHandler(SpringContextUtil.getBean("taskExceptionHandler"));
            listenerThread = new ListenerThread();
            onlineUsers = new AtomicInteger(0);
            interruptedExceptionHandler = SpringContextUtil.getBean("interruptedExceptionHandler");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动方法，线程最好不要在构造函数中启动，应该作为一个单独方法，或者使用工厂方法来创建实例
     * 避免构造未完成就使用成员变量
     */
    public void launch(){
        new Thread(listenerThread).start();
        new Thread(taskManagerThread).start();
    }

    /**
     * 推荐的结束线程的方式是使用中断
     * 在while循环开始处检查是否中断，并提供一个方法来将自己中断
     * 不要在外部将线程中断
     * <p>
     * 另外，如果要中断一个阻塞在某个地方的线程，最好是继承自Thread，先关闭所依赖的资源，再关闭当前线程
     */
    private class ListenerThread extends Thread {
        @Override
        public void run() {
            try{
                //如果有一个及以上的客户端的数据准备就绪
                while (!Thread.currentThread().isInterrupted()){
                    //当注册的事件到达时，方法返回；否则,该方法会一直阻塞
                    selector.select();
                    //获取当前选择器中所有注册的监听事件
                    for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext();){
                        SelectionKey key = it.next();
                        //每次select后selector只会在selectedKeys集合把新就绪的事件add，而不会删除或清空selectedKeys集合,所以需要手动删除已选的key,以防下次会重复处理
                        it.remove();
                        //如果"接收"事件已就绪
                        if (key.isAcceptable()){
                            //交由接收事件的处理器处理
                            handleAcceptRequest();
                        }else if (key.isReadable()){
                            //如果"读取"事件已就绪
                            //取消可读触发标记，本次处理完后才打开读取事件标记
                            key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                            //交由读取事件的处理器处理
                            readPool.execute(new ReadEventHandler(key));
                        }
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        @Override
        public void interrupt() {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                super.interrupt();
            }
        }

        public void shutdown(){
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 处理客户端的连接请求
     */
    private void handleAcceptRequest(){
        try {
            SocketChannel client = serverSocketChannel.accept();
            // 接收的客户端也要切换为非阻塞模式
            client.configureBlocking(false);
            // 监控客户端的读操作是否就绪
            client.register(selector, SelectionKey.OP_READ);
//            log.info("服务器连接客户端:{}",client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处于线程池中的线程会随着线程池的shutdown方法而关闭
     */
    private class ReadEventHandler implements Runnable{

        private ByteBuffer buf;
        private SocketChannel client;
        private SelectionKey key;


        public ReadEventHandler(SelectionKey key){
            this.key = key;
            this.client = (SocketChannel) key.channel();
            this.buf = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
        }

        @Override
        public void run() {
            try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                int size;
                while ((size = client.read(buf)) > 0){
                    buf.flip();
                    baos.write(buf.array(), 0, size);
                    buf.clear();
                }

                //连接关闭，结束
                if (size == -1)
                    return;

//                log.info("读取完毕，继续监听");
                //继续监听读取事件
                key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                key.selector().wakeup();
                byte[] bytes = baos.toByteArray();
                Message message = ProtoStuffUtil.deserialize(bytes, Message.class);
                MessageHandler messageHandler = SpringContextUtil.getBean("MessageHandler", message.getHeader().getType().toString().toLowerCase());

                try{
                    messageHandler.handle(message, selector, key, downloadTaskQueue, onlineUsers);
                }catch (InterruptedException e){
//                    log.error("服务器线程被中断");
                    interruptedExceptionHandler.handle(client, message);
                    e.printStackTrace();
                }

            }catch (IOException e){

            }
        }
    }

    public static void main(String[] args){
        System.out.println("Initialing...");
        ChatServer chatServer = new ChatServer();

    }
}
