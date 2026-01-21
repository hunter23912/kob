package com.kob.backend.service.impl.pk;

import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.consumer.utils.Game;
import com.kob.backend.service.pk.ReceiveBotMoveService;
import org.springframework.stereotype.Service;

@Service
public class ReceiveBotMoveServiceImpl implements ReceiveBotMoveService {
    @Override
    public String receiveBotMove(Integer userId, Integer direction) {
        System.out.println("receive bot move: userId = " + userId + ", direction = " + direction);

        // java 21特性
        var user = WebSocketServer.users.get(userId);
        if(user != null && user.game != null) {
            Game game = user.game;
            // Game game = WebSocketServer.users.get(userId).game;
            if(game.getPlayerA().getId().equals(userId)) {
                game.setNextStepA(direction);
            } else if(game.getPlayerB().getId().equals(userId)) {
                game.setNextStepB(direction);
            }
        }
        return "receive bot move success";
    }
}
