package etsheep.student.chatserver.task;

import etsheep.student.chat.common.domain.Task;
import etsheep.student.chatserver.handler.task.BaseTaskHandler;
import etsheep.student.chatserver.http.HttpConnectionManager;
import etsheep.student.chatserver.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by etsheep on 2019-7-19.
 * 消费者
 * 负责从阻塞队列中取出任务并提交给线程池
 */

@Slf4j
public class TaskManagerThread extends Thread{

    private ExecutorService taskPool;
    private BlockingQueue<Task> taskBlockingQueue;
    private HttpConnectionManager httpConnectionManager;

    private ExecutorService crawlerPool;

    public TaskManagerThread(BlockingQueue<Task> taskBlockingQueue){
        this.taskPool = new ThreadPoolExecutor();
        this.taskBlockingQueue = taskBlockingQueue;
        this.httpConnectionManager = SpringContextUtil.getBean("httpConnectionManager");
        this.crawlerPool = new ThreadPoolExecutor();
    }

    public void shutdown(){
        taskPool.shutdown();
        crawlerPool.shutdown();
        Thread.currentThread().interrupt();
    }


    /**
     * 如果当前线程被中断，那么Future会抛出InterruptedException，
     * 此时可以通过future.cancel(true)来中断当前线程
     * <p>
     * 由submit方法提交的任务中如果抛出了异常，那么会在ExecutionException中重新抛出
     */
    @Override
    public void run() {
        try{
            while (!Thread.currentThread().isInterrupted()){
                Task task = taskBlockingQueue.take();
//                log.info("{}已从阻塞队列中取出",task.getReceiver().getRemoteAddress());
                BaseTaskHandler taskHandler = SpringContextUtil.getBean("BaseTaskHandler", task.getType().toString().toLowerCase());
                taskHandler.init(task, httpConnectionManager, this);
                System.out.println(taskHandler);
                taskPool.execute(taskHandler);
            }
        }catch (InterruptedException e){
            //这里也无法得知发来消息的是谁，所以只能直接退出了
            e.printStackTrace();
        }
    }

    public ExecutorService getCrawlerPool(){
        return crawlerPool;
    }
}
