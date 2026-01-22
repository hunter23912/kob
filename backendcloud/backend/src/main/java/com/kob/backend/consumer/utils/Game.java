package com.kob.backend.consumer.utils;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.Record;
import lombok.Getter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Game extends Thread{
    private final Integer rows;
    private final Integer cols;
    private final Integer inner_walls_count;
    @Getter
    private final int[][] g;
    private static final int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
    @Getter
    private final Player playerA, playerB;

    private Integer nextStepA = null;
    private Integer nextStepB = null; // 玩家下一步操作方向

    private final ReentrantLock lock = new ReentrantLock();
    private String status = "playing";
    private String loser = ""; // all平局，A玩家输，B玩家输
    private static final String addBotUrl = "http://localhost:8082/bot/add/";


    public Game(Integer rows, Integer cols, Integer inner_walls_count, Integer idA, Bot botA, Integer idB, Bot botB) {
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.g = new int[rows][cols];
        Integer botIdA = -1, botIdB = -1;
        String botCodeA = "", botCodeB = "";


        if(botA != null) {
            botIdA = botA.getId();
            botCodeA = botA.getContent();
        }
        if(botB != null) {
            botIdB = botB.getId();
            botCodeB = botB.getContent();
        }
        playerA = new Player(idA, botIdA, botCodeA, rows - 2, 1, new ArrayList<>());
        playerB  = new Player(idB, botIdB, botCodeB, 1, cols - 2, new ArrayList<>());
    }

    public void setNextStepA(Integer nextStepA) {
        lock.lock();
        try {
            this.nextStepA = nextStepA;
        } finally {
            lock.unlock();
        }
    }

    public void setNextStepB(Integer nextStepB) {
        lock.lock();
        try {
            this.nextStepB = nextStepB;
        } finally {
            lock.unlock();
        }
    }

    private boolean check_connectivity(int sx, int sy, int tx, int ty) {
        if (sx == tx && sy == ty) return true;
        g[sx][sy] = 1;

        for (int i = 0; i < 4; i++) {
            int x = sx + dx[i], y = sy + dy[i];
            if(x > 0 && x < rows && y > 0 && y < cols && g[x][y] == 0) {
                if (check_connectivity(x, y, tx, ty)) {
                    g[sx][sy] = 0;
                    return true;
                }
            }
        }
        g[sx][sy] = 0;

        return false;
    }

    private boolean draw() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                g[i][j] = 0; // 0代表空地
            }
        }

        for (int r = 0; r < rows; r ++ ) {
            g[r][0] = g[r][cols - 1] = 1; // 1代表墙
        }
        for (int c = 0; c < cols; c ++ ) {
            g[0][c] = g[rows - 1][c] = 1;
        }

        Random random = new Random();
        for(int i = 0; i < inner_walls_count / 2; i ++ ) {
            for(int j = 0; j < 1000; j ++ ) {
                int r = random.nextInt(rows);
                int c = random.nextInt(cols);
                if(g[r][c] == 1 || g[rows - 1 - r][cols - 1 - c] == 1) continue;
                if((r == 1 && c == cols - 2) || (c == 1 && r == rows - 2 )) continue;
                g[r][c] = g[rows - 1 - r][cols - 1 - c] = 1;
                break;
            }
        }
        return check_connectivity(rows - 2, 1, 1, cols - 2);
    }

    public void createMap() {
        for (int i = 0; i < 1000; i++) {
            if (draw())
                break;
        }
    }

    private String getInput(Player player) {
        Player me, you;
        if(player.getId().equals(playerA.getId())) {
            me = playerA;
            you = playerB;
        } else {
            me = playerB;
            you = playerA;
        }
        return getMapString() + "#" +
                me.getSx() + "#" +
                me.getSy() + "#(" +
                me.getStepsString() + ")#" +
                you.getSx() + "#" +
                you.getSy() + "#(" +
                you.getStepsString() + ")";
    }

    private void sendBotCode(Player player) {
        if (player.getBotId().equals(-1)) return; // 人工操作不需要发送代码
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", player.getId().toString());
        data.add("bot_code", player.getBotCode());
        data.add("input", getInput(player));
        WebSocketServer.restTemplate.postForObject(addBotUrl, data, String.class); // 发送给AI玩家的微服务
    }

    private boolean nextStep() { // 等待两名玩家的下一步操作
        try { // 前端200ms移动一格，保证与前端同步
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 先假定派出的是AI玩家，发送代码和输入
        sendBotCode(playerA);
        sendBotCode(playerB);
        // 最多等待5秒钟
        for(int i = 0; i < 50; i ++ ) {
            try {
                Thread.sleep(100);
                lock.lock();
                try {
                    if(nextStepA != null && nextStepB != null) { // 两名玩家都已经操作
                        playerA.getSteps().add(nextStepA);
                        playerB.getSteps().add(nextStepB);
                        return true;
                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    private boolean check_valid(List<Cell> cellsA, List<Cell> cellsB) { // 判断某名玩家的操作是否合法
        int n = cellsA.size();
        int m = cellsB.size();
        Cell cell = cellsA.get(n - 1); // 玩家下一步操作后，蛇头所在的位置
        if(g[cell.x][cell.y] == 1) return false; // 撞墙

        for (int i = 0; i < n - 1; i++) { // 排除蛇头
            Cell t = cellsA.get(i);
            if (cell.x == t.x && cell.y == t.y) return false; // 撞到自己
        }

        for (int i = 0; i < m; i++) { // 这里要判断对方的蛇头
            Cell t = cellsB.get(i);
            if (cell.x == t.x && cell.y == t.y) return false; // 撞到对方
        }
        return true;
    }

    private void judge() { // 判断两名玩家下一步操作是否合法
        List<Cell> cellsA = playerA.getCells();
        List<Cell> cellsB = playerB.getCells();

        boolean validA = check_valid(cellsA, cellsB);
        boolean validB = check_valid(cellsB, cellsA);
        if(!validA || !validB) {
            status = "finished";
            if(!validA && !validB) {
                loser = "all";
            } else if(!validA) {
                loser = "A";
            } else {
                loser = "B";
            }
        }
    }

    private void sendAllMessage(String message) { // 向两个Client发送消息
        if(WebSocketServer.users.get(playerA.getId()) != null) {
            WebSocketServer.users.get(playerA.getId()).sendMessage(message);
        }
        if(WebSocketServer.users.get(playerB.getId()) != null) {
            WebSocketServer.users.get(playerB.getId()).sendMessage(message);
        }
    }

    private void sendMove() { // 向两个Client公布两名玩家的下一步操作
        lock.lock();
        try {
            JSONObject resp = new JSONObject();
            resp.put("event", "move");
            resp.put("a_direction", nextStepA);
            resp.put("b_direction", nextStepB);
            sendAllMessage(resp.toJSONString());
            nextStepA = nextStepB = null; // 重置下一步操作
        } finally {
            lock.unlock();
        }
    }

    private String getMapString() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                res.append(g[i][j]);
            }
        }
        return res.toString();
    }

    private void saveToDatabase() {
        Record record = new Record (
            null,
            playerA.getId(),
            playerA.getSx(),
            playerA.getSy(),
            playerB.getId(),
            playerB.getSx(),
            playerB.getSy(),
            playerA.getStepsString(),
            playerB.getStepsString(),
            getMapString(),
            loser,
            new Date()
        );
        WebSocketServer.recordMapper.insert(record);
    }

    private  void sendResult() { // 向两个Client公布结果
        JSONObject resp = new JSONObject();
        resp.put("event", "result");
        resp.put("loser", loser);
        saveToDatabase();
        sendAllMessage(resp.toJSONString());
    }

    @Override
    public void run() { // 新线程的入口函数
        for (int i = 0; i < 1000; i ++ ) { // 两名玩家最多一共进行1000步操作
            if(nextStep()) { // 是否获取了两名玩家的下一步操作
                judge();
                if(status.equals("playing")) {
                    sendMove();
                } else {
                    sendResult();
                    break;
                }
            } else { // 有玩家超时未操作
                status = "finished";
                lock.lock();
                try {
                    if(nextStepA == null && nextStepB == null) {
                        loser = "all";
                    } else if(nextStepA == null) {
                        loser = "A";
                    } else {
                        loser = "B";
                    }
                } finally {
                    lock.unlock();
                }
                try {
                    Thread.sleep(200); // 等待200ms，保证前端能够收到最后的move消息
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                sendResult();
                break;
            }
        }
    }
}
