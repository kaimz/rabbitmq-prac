package com.wuwii;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootAnnotationApplicationTests {

    @Autowired
    private MessageSender sender;

    @Test
    public void send() {
        sender.send();
    }

    @Test
    public void sendAndReceive() {
        sender.sendAndReceive();
    }
}
