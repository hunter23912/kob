package com.kob.botrunningsystem.service.impl.utils;

import com.kob.botrunningsystem.utils.BotInterface;
import org.joor.Reflect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Consumer extends Thread{
    private Bot bot;
    private static RestTemplate restTemplate;
    private static final String receiveBotMoveUrl = "http://127.0.0.1:8080/pk/receive/bot/move/";

    private static final Map<String, Class<?>> cache = new ConcurrentHashMap<>();

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        Consumer.restTemplate = restTemplate;
    }

    public void startTimeout(long timeout, Bot bot) {
        this.bot = bot;
        this.start();

        try {
            this.join(timeout); // 当start的线程执行完后，或者等待timeout毫秒后，继续往下执行
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.interrupt(); // 中断当前线程，防止用户程序死循环
        }
    }

    private String addUuid(String code, String uid) { // 在code中的Bot类名后添加uid
        int k = code.indexOf(" implements com.kob.botrunningsystem.utils.BotInterface");
        return code.substring(0, k) + uid + code.substring(k);

    }

    @Override
    public void run() { // 带缓存的版本，极大提升性能
        try {
            // 1. 计算当前代码的哈希值
            String code = bot.getBotCode();
            String codeHash = DigestUtils.md5DigestAsHex(code.getBytes());

            BotInterface botInterface;

            // 2. 检查缓存
            if (cache.containsKey(codeHash)) {
                // 如果缓存命中，直接通过反射创建实例（耗时几乎为 0）
                botInterface = (BotInterface) cache.get(codeHash).getDeclaredConstructor().newInstance();
            } else {
                // 如果缓存未命中，进行重型编译操作
                UUID uuid = UUID.randomUUID();
                String uid = uuid.toString().substring(0, 8);

                Class<?> clazz = Reflect.compile(
                        "com.kob.botrunningsystem.utils.Bot" + uid,
                        addUuid(code, uid)
                ).type(); // 获取编译后的 Class 类型

                cache.put(codeHash, clazz); // 存入缓存
                botInterface = (BotInterface) clazz.getDeclaredConstructor().newInstance();
            }

            // 3. 执行逻辑
            Integer direction = botInterface.nextMove(bot.getInput());

            System.out.println("move: user: " + bot.getUserId() + " direction: " + direction);
            MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
            data.add("user_id", bot.getUserId().toString());
            data.add("direction", direction.toString());

            restTemplate.postForObject(receiveBotMoveUrl, data, String.class);

        } catch (Exception e) {
            // 注意：这里需要捕获反射或编译可能产生的异常
            e.printStackTrace();
        }
    }

    /* 旧版，不带缓存
    @Override
    public void run1() {
        UUID uuid = UUID.randomUUID();
        String uid = uuid.toString().substring(0, 8);
        BotInterface botInterface = Reflect.compile(
                "com.kob.botrunningsystem.utils.Bot" + uid,
                addUuid(bot.getBotCode(), uid)
        ).create().get();

        Integer direction = botInterface.nextMove(bot.getInput());

        System.out.println("move: user: " + bot.getUserId() + " direction: " + direction);
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", bot.getUserId().toString());
        data.add("direction", direction.toString());

        restTemplate.postForObject(receiveBotMoveUrl, data, String.class);
    }
    */
}
