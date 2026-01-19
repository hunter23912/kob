import { AcGameObject } from "./AcGameObject";
import { Snake } from "./Snake";
import { Wall } from "./Wall";

export class GameMap extends AcGameObject {
  constructor(ctx, parent, pkStore) {
    super();
    this.ctx = ctx;
    this.parent = parent;
    this.L = 0;
    this.pkStore = pkStore;

    this.rows = 15;
    this.cols = 15;

    this.inner_walls_count = 20; // 墙的数量
    this.walls = [];

    this.snakes = [
      new Snake({ id: 0, color: "#4876EC", r: this.rows - 2, c: 1 }, this),
      new Snake({ id: 1, color: "#F94848", r: 1, c: this.cols - 2 }, this),
    ];
  }

  create_walls() {
    const g = this.pkStore.gamemap_array; // 从store中获取地图数据
    // 生成墙体
    for (let r = 0; r < this.rows; r++) {
      for (let c = 0; c < this.cols; c++) {
        if (g[r][c]) {
          this.walls.push(new Wall(r, c, this));
        }
      }
    }
    return true;
  }

  add_listening_events() {
    this.ctx.canvas.focus();

    this.ctx.canvas.addEventListener("keydown", (e) => {
      let d = -1;
      if (e.key === "w") d = 0;
      else if (e.key === "d") d = 1;
      else if (e.key === "s") d = 2;
      else if (e.key === "a") d = 3;

      if (d >= 0) {
        this.pkStore.socket.send(
          JSON.stringify({
            event: "move",
            direction: d,
          }),
        );
      }
    });
  }

  start() {
    this.create_walls();
    this.add_listening_events();
  }

  update_size() {
    this.L = parseInt(Math.min(this.parent.clientWidth / this.cols, this.parent.clientHeight / this.rows));
    this.ctx.canvas.width = this.L * this.cols;
    this.ctx.canvas.height = this.L * this.rows;
  }

  check_ready() {
    for (const snake of this.snakes) {
      if (snake.status !== "idle") return false;

      if (snake.direction === -1) return false;
    }
    return true;
  }

  next_step() {
    // 所有蛇进入下一回合
    for (const snake of this.snakes) {
      snake.next_step();
    }
  }

  check_valid(cell) {
    for (const wall of this.walls) {
      if (wall.r === cell.r && wall.c === cell.c) return false;
    }

    for (const snake of this.snakes) {
      let k = snake.cells.length;

      // 当蛇尾会前进时候，少判断一节
      if (!snake.check_tail_increasing()) k--;

      for (let i = 0; i < k; i++) {
        if (snake.cells[i].r === cell.r && snake.cells[i].c === cell.c) return false;
      }
    }
    return true;
  }

  update() {
    this.update_size();
    if (this.check_ready()) {
      this.next_step();
    }
    this.render();
  }

  render() {
    const color_even = "rgb(53, 192, 99)";
    const color_odd = "rgb(28, 139, 91)";
    for (let c = 0; c < this.cols; c++) {
      for (let r = 0; r < this.rows; r++) {
        if ((r + c) % 2 == 0) {
          this.ctx.fillStyle = color_even;
        } else {
          this.ctx.fillStyle = color_odd;
        }
        this.ctx.fillRect(c * this.L, r * this.L, this.L, this.L);
      }
    }
  }
}
