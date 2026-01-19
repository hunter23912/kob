import { AcGameObject } from "./AcGameObject";
import { Cell } from "./Cell";

export class Snake extends AcGameObject {
  constructor(info, gamemap) {
    super();

    this.id = info.id;
    this.color = info.color;
    this.gamemap = gamemap;
    this.cells = [new Cell(info.r, info.c), new Cell(info.r, info.c)]; // 存放蛇的头部和尾部，避免初次移动时单个圆滑动
    this.next_cell = null; // 下一步的目标位置

    this.speed = 5; // 蛇每秒钟走多少格子

    this.direction = -1; // -1表示没有方向，0表示上，1表示右，2表示下，3表示左
    this.status = "idle"; // idle表示静止，move表示正在移动，die表示死亡

    this.dr = [-1, 0, 1, 0]; // 行的偏移量
    this.dc = [0, 1, 0, -1]; // 列的偏移量

    this.step = 0; // 回合数

    this.eps = 1e-2; // 误差范围

    this.eye_direction = 0; // 眼睛方向
    if (this.id === 1) this.eye_direction = 2; // 默认第二条蛇向下

    this.eye_dx = [
      [-1, 1],
      [1, 1],
      [1, -1],
      [-1, -1],
    ];

    this.eye_dy = [
      [-1, -1],
      [-1, 1],
      [1, 1],
      [1, -1],
    ];
  }

  start() {}

  set_direction(d) {
    this.direction = d;
  }

  check_tail_increasing() {
    // 检测当前回合蛇的长度是否增加
    if (this.step <= 10) return true;
    return this.step % 3 === 1;
  }

  next_step() {
    // 只设置目标，不直接插入新头
    const d = this.direction;
    this.next_cell = new Cell(this.cells[0].r + this.dr[d], this.cells[0].c + this.dc[d]);
    this.eye_direction = d;
    this.direction = -1; // 清空方向
    this.status = "move";
    this.step += 1;

    // 这是链式移动法
    // const k = this.cells.length;
    // for (let i = k; i > 0; i--) {
    //   // 0是蛇头
    //   this.cells[i] = JSON.parse(JSON.stringify(this.cells[i - 1])); // 深拷贝
    // }

    // 后端判断
    // if (!this.gamemap.check_valid(this.next_cell)) {
    //   // 下一步撞墙或撞到自己，死亡
    //   this.status = "die";
    // }
  }

  update_move() {
    // 让蛇的移动变得平滑自然
    const dx = this.next_cell.x - this.cells[0].x;
    const dy = this.next_cell.y - this.cells[0].y;

    const distance = Math.sqrt(dx * dx + dy * dy);

    if (distance < this.eps) {
      // 到达目标点，插入新头，删除尾部
      this.cells.unshift(this.next_cell);
      this.next_cell = null;
      this.status = "idle";
      if (!this.check_tail_increasing()) {
        // 蛇不变长
        this.cells.pop();
      }
    } else {
      // 没到达目标点，继续移动
      const move_distance = (this.speed * this.timedelta) / 1000; // 每一帧走多少距离

      // 头部的移动效果
      this.cells[0].x += (move_distance * dx) / distance;
      this.cells[0].y += (move_distance * dy) / distance;

      // 如果不在变长，尾部需要绘制移动效果
      if (!this.check_tail_increasing()) {
        const tail = this.cells.at(-1);
        const tail_target = this.cells.at(-2);
        const tail_dx = tail_target.x - tail.x;
        const tail_dy = tail_target.y - tail.y;
        tail.x += (move_distance * tail_dx) / distance;
        tail.y += (move_distance * tail_dy) / distance;
      }
    }
  }

  update() {
    // 每一帧执行一次，一般每秒执行60次
    if (this.status === "move") {
      this.update_move();
    }
    this.render();
  }

  render() {
    const L = this.gamemap.L;
    const ctx = this.gamemap.ctx;

    ctx.fillStyle = this.color;

    if (this.status === "die") {
      ctx.fillStyle = "white";
    }

    for (const cell of this.cells) {
      ctx.beginPath();
      ctx.arc(cell.x * L, cell.y * L, (L / 2) * 0.8, 0, Math.PI * 2);
      ctx.fill();
    }

    for (let i = 1; i < this.cells.length; i++) {
      const a = this.cells[i - 1],
        b = this.cells[i];
      if (Math.abs(a.x - b.x) < this.eps && Math.abs(a.y - b.y) < this.eps) {
        continue;
      }
      if (Math.abs(a.x - b.x) < this.eps) {
        // 绘制竖直矩形
        ctx.fillRect((a.x - 0.4) * L, Math.min(a.y, b.y) * L, L * 0.8, Math.abs(a.y - b.y) * L);
      } else {
        // 绘制水平矩形
        ctx.fillRect(Math.min(a.x, b.x) * L, (a.y - 0.4) * L, Math.abs(a.x - b.x) * L, L * 0.8);
      }
    }

    // 绘制眼睛
    ctx.fillStyle = "black";
    for (let i = 0; i < 2; i++) {
      const eye_x = (this.cells[0].x + this.eye_dx[this.eye_direction][i] * 0.15) * L;
      const eye_y = (this.cells[0].y + this.eye_dy[this.eye_direction][i] * 0.15) * L;
      ctx.beginPath();
      ctx.arc(eye_x, eye_y, L * 0.05, 0, Math.PI * 2);
      ctx.fill();
    }
  }
}
