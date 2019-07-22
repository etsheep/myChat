package etsheep.student.chat.common.enumeration;

/**
 * Created by etsheep on 2019-7-19.
 */
public enum TaskType {
    FILE(1,"文件"),
    CRAWL_IMAGE(2,"豆瓣电影图片");
    private int code;
    private String desc;

    TaskType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
}
