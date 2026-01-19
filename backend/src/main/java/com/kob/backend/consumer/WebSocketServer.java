package com.kob.backend.consumer;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.utils.Game;
import com.kob.backend.consumer.utils.JwtAuthentication;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/websocket/{token}") // 注意不要加斜杠，否则会报错
public class WebSocketServer {

    final static ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();

    final private static CopyOnWriteArraySet<User> matchpool = new CopyOnWriteArraySet<>();

    private User user;

    private Session session = null;

    private static UserMapper userMapper;

    @Autowired // 非单例模式下无法自动注入，需要使用static方法注入
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        this.session = session;
        System.out.println("connected!");
        Integer userId = JwtAuthentication.getUserId(token);
        this.user = userMapper.selectById(userId);

        if(this.user != null) {
            users.put(userId, this);
        } else {
            this.session.close();
        }
        System.out.println(users);
    }

    @OnClose
    public void onClose() {
        // 关闭连接
        System.out.println("disconnected!");
        if(this.user != null) {
            users.remove(this.user.getId());
            matchpool.remove(this.user);
        }
    }

    private void startMatching() {
        System.out.println("start matching");
        matchpool.add(this.user);

        while(matchpool.size() >= 2) {
            Iterator<User> it = matchpool.iterator();
            User a = it.next(), b = it.next();
            matchpool.remove(a);
            matchpool.remove(b);

            Game game = new Game(15,15,20);
            game.createMap();

            sendMatchResult(b, a, game);

            sendMatchResult(a, b, game);
        }
    }

    private void sendMatchResult(User user, User opponent, Game game) {
        JSONObject respUser = new JSONObject();
        respUser.put("event", "match-success");
        respUser.put("opponent_username", opponent.getUsername());
        respUser.put("opponent_photo", opponent.getPhoto());
        respUser.put("gamemap", game.getG());
        users.get(user.getId()).sendMessage(respUser.toJSONString()); // 发送给前端
    }

    private void stopMatching() {
        System.out.println("stop matching");
        matchpool.remove(this.user);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 从Client接收消息
        System.out.println("Received message: " + message);
        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");
        if("start-matching".equals(event)) {
            startMatching();
        } else if("stop-matching".equals(event)) {
            stopMatching();
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        // 处理错误
        System.out.println("WebSocket error: " + error.getMessage());
    }

    public void sendMessage(String message) {
        synchronized (this.session) {
            try {
                this.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
