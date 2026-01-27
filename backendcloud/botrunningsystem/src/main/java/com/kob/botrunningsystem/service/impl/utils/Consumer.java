package com.kob.botrunningsystem.service.impl.utils;

import org.joor.Reflect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
public class Consumer extends Thread{
    private Bot bot;
    private static RestTemplate restTemplate;

    private static String receiveBotMoveUrl;

    private static final Map<String, Class<?>> cache = new ConcurrentHashMap<>();

    @Value("${game.url.botmove}")
    public void setReceiveBotMoveUrl(String url) {
        Consumer.receiveBotMoveUrl = url;
    }

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
        int k = code.indexOf(" implements java.util.function.Supplier<Integer>");
        return code.substring(0, k) + uid + code.substring(k);

    }

    @Override
    public void run() { // 带缓存的版本，极大提升性能
        try {
            // 1. 计算当前代码的哈希值
            String code = bot.getBotCode();
            String codeHash = DigestUtils.md5DigestAsHex(code.getBytes());

            Supplier<Integer> botInterface;
            Class<?> clazz; // 用来接收编译后的Bot123类
            // 2. 检查缓存
            if (cache.containsKey(codeHash)) {
                // 如果缓存命中，直接通过反射创建实例（耗时几乎为 0）
                clazz = cache.get(codeHash);
            } else {
                // 如果缓存未命中，进行重型编译操作
                UUID uuid = UUID.randomUUID();

                String uid = uuid.toString().substring(0, 8);
                String fullClassName = "com.kob.botrunningsystem.utils.Bot" + uid;

                String processCode = addUuid(code, uid);

                clazz = Reflect.compile(
                        fullClassName,
                        processCode
                ).type(); // 获取编译后的 Class 类型

                cache.put(codeHash, clazz); // 存入缓存
            }
            botInterface = (Supplier<Integer>) clazz.getDeclaredConstructor().newInstance();

            // 将输入传入到一个文件中，支持多语言脚本
            File file = new File("input.txt");
            try(PrintWriter fout = new PrintWriter(file)) {
                fout.println(bot.getInput());
                fout.flush(); // 将内存缓冲区的数据强制写入到磁盘文件input.txt中
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            // 3. 执行逻辑
            Integer direction = botInterface.get();

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
}
