package com.kob.backend.consumer.utils;

import lombok.Getter;

import java.util.Random;

public class Game {
    final private Integer rows;
    final private Integer cols;
    final private Integer inner_walls_count;
    @Getter
    final private int[][] g;
    final private static int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};

    public Game(Integer rows, Integer cols, Integer inner_walls_count) {
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.g = new int[rows][cols];
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
}
