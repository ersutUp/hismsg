package xyz.ersut.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 消息系统启动类
 * 
 * @author ersut
 */
@SpringBootApplication
@MapperScan("xyz.ersut.message.mapper")
public class MessageSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessageSystemApplication.class, args);
    }

}