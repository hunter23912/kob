### 如何极速部署网站？

#### 大致流程

```text
浏览器 → HTTPS(443) → Nginx(反向代理+静态文件) → Gunicorn(多进程) → Django(wsgi)
```

#### 具体步骤

```bash
# 安装nginx
sudo apt install nginx -y

# 安装gunicorn
pip install gunicorn

# 在项目根目录下，用gunicorn启动Django项目
gunicorn --bind 127.0.0.1:8080 acapp.wsgi:application

# 配置nginx反向代理
sudo vim /etc/nginx/sites-available/django
```

##### nginx 配置文件内容

```nginx
server {
    server_name ballgame.jaxenwang.top;   # 只匹配这个子域名

    # 所有请求转发给 Gunicorn（用 unix socket）
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 静态文件直接由 Nginx 处理（性能最好）
    location /static/ {
        alias /var/www/ballgame/static/;   # 改成你的实际路径
    }

    listen 443 ssl; # managed by Certbot
    ssl_certificate /etc/letsencrypt/live/ballgame.jaxenwang.top/fullchain.pem; # managed by Certbot
    ssl_certificate_key /etc/letsencrypt/live/ballgame.jaxenwang.top/privkey.pem; # managed by Certbot
    include /etc/letsencrypt/options-ssl-nginx.conf; # managed by Certbot
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # managed by Certbot

}
server {
    if ($host = ballgame.jaxenwang.top) {
        return 301 https://$host$request_uri;
    } # managed by Certbot


    listen 80;
    server_name ballgame.jaxenwang.top;
    return 404; # managed by Certbot


}
```

```shell
# 测试nginx配置是否正确
sudo nginx -t
# 启用配置
sudo ln -s /etc/nginx/sites-available/django /etc/nginx/sites-enabled
# 重启nginx
sudo systemctl restart nginx
```

##### 可能遇见文件权限的问题，可将静态文件转移或者修改权限

- 转移静态文件

```bash
cp -r /root/acapp/game/static/* /var/www/ballgame/static/
# nginx默认用户是www-data
chown -R www-data:www-data /var/www/ballgame/static
chmod -R 755 /var/www/ballgame/static
```

#### 多个微服务部署示例（主项目 8000 端口，websocket 8001 端口，匹配系统微服务 8002 端口）

```bash
vim /etc/nginx/sites-available/django

server {
    server_name ballgame.jaxenwang.top;   # 只匹配这个子域名

    # 所有请求转发给 Gunicorn（用 unix socket）
    location / {
        proxy_pass http://127.0.0.1:8000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 静态文件直接由 Nginx 处理（性能最好）
    location /static/ {
        alias /var/www/ballgame/static/;   # 改成你的实际路径
    }

    listen 443 ssl; # managed by Certbot
    ssl_certificate /etc/letsencrypt/live/ballgame.jaxenwang.top/fullchain.pem; # managed by Certbot
    ssl_certificate_key /etc/letsencrypt/live/ballgame.jaxenwang.top/privkey.pem; # managed by Certbot
    include /etc/letsencrypt/options-ssl-nginx.conf; # managed by Certbot
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # managed by Certbot

}
server {
    if ($host = ballgame.jaxenwang.top) {
        return 301 https://$host$request_uri;
    } # managed by Certbot


    listen 80;
    server_name ballgame.jaxenwang.top;
    return 404; # managed by Certbot


}
```

#### 使用 systemd 管理服务

- `match` 服务

```bash
sudo vim /etc/systemd/system/ballgame-match.service
```

##### `ballgame-match.service` 内容

```ini
[Unit]
Description=Ballgame Match Microservice
After=network.target

[Service]
User=root
Group=root
WorkingDirectory=/root/acapp/match_system/src
Environment="PYTHONUNBUFFERED=1"
# 使用绝对路径指向 Python 解释器
ExecStart=/root/acapp/match_system/src/main.py
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

- `web` 服务

```bash
sudo vim /etc/systemd/system/ballgame-web.service
```

##### `ballgame-web.service` 内容

```ini
[Unit]
Description=Ballgame Django Web (daphne)
After=network.target

[Service]
User=root
Group=root
WorkingDirectory=/root/acapp
# 环境变量确保 Python 输出不被缓存，方便查看日志
Environment="PYTHONUNBUFFERED=1"
# 这里的路径请确保是指向你虚拟环境或系统 Python 的 gunicorn
ExecStart=/root/.pyenv/shims/daphne -b 127.0.0.1 -p 8000 acapp.asgi:application
Restart=always
RestartSec=3

[Install]
WantedBy=multi-user.target
```

#### 看 systemd 服务日志

```bash
journalctl -u ballgame-web -n 20 --no-pager
```

#### 刷新 systemd 配置并启动服务

```bash
sudo systemctl daemon-reload
```

### 多项目部署方式
- 公网流量到达服务器`80/443`端口
- 宿主机`nginx`接收请求，识别`server_name`
- 根据不同子域名，执行`proxy_pass`到不同端口
- 根据`docker`端口映射，转发到不同容器内的服务
- 容器内服务处理请求，返回响应

- 最终部署项目形式如下：

  ```bash
  ./kob/
  ├── backend.jar
  ├── botrunningsystem.jar
  ├── docker-compose.yml
  ├── matchsystem.jar
  └── web
      ├── assets
      │   ├── background-BInJ3AyV.jpg
      │   ├── index-3YwQeF6I.css
      │   └── index-B0cQU344.js
      ├── favicon.ico
      └── index.html
  ```

- 采用`docker compose`运行三个微服务，`docker compose`运行不同容器位于同一个虚拟网络中，可以通过容器名作为域名来进行`RPC`调用。

- 其中`web`是前端`vue`构建的代码，前端`vue`打包命令：

  ```bash
  pnpm run build
  ```

  然后生成dist文件夹，将其中所有文件传到服务器项目根目录中的`web`目录下：

  ```bash
  cd dist
  scp -r ./* Server:/home/jax/kob/web/
  ```

- `jar`包是三个后端微服务，`docker`启动脚本为`yml`文件。`yml`文件内容如下：

  ```yml
  services:
    kob-backend:
      image: eclipse-temurin:21-jdk-alpine
      container_name: kob-backend
      volumes:
        - ./backend.jar:/app/backend.jar
      working_dir: /app
      environment:
        - MATCH_SYSTEM_URL=http://kob-matchsystem:8081
        - BOTRUNNING_SYSTEM_URL=http://kob-botrunningsystem:8082
      command: java -Xmx512m -Xms256m -jar backend.jar
      ports:
        - "8080:8080"
      restart: always
  
    kob-matchsystem:
      image: eclipse-temurin:21-jdk-alpine
      container_name: kob-matchsystem
      volumes:
        - ./matchsystem.jar:/app/matchsystem.jar
      working_dir: /app
      environment:
        - GAME_SYSTEM_URL=http://kob-backend:8080
      command: java -Xmx256m -Xms128m -jar matchsystem.jar
      ports:
        - "8081:8081"
      restart: always
  
    kob-botrunningsystem:
      image: eclipse-temurin:21-jdk-alpine
      container_name: kob-botrunningsystem
      volumes:
        - ./botrunningsystem.jar:/app/botrunningsystem.jar
      working_dir: /app
      environment:
        - GAME_SYSTEM_URL=http://kob-backend:8080
      # BotRunningSystem 需要 JDK 进行动态编译，此镜像已包含
      command: java -Xmx256m -Xms128m -jar botrunningsystem.jar
      ports:
        - "8082:8082"
      restart: always
  ```

  - 该项目通过3个docker运行3个微服务，8080是后端总入口，8081是匹配系统，8082是AI代码编译执行系统。

  - 通过设置`environmnet`将各个容器名作为`RPC`的域名地址，然后`spring boot`中在`application.properties`中读取环境变量：

    ```ini
    matching.url.add=${MATCH_SYSTEM_URL}/player/add/
    matching.url.remove=${MATCH_SYSTEM_URL}/player/remove/
    botrunning.url.add=${BOTRUNNING_SYSTEM_URL}/bot/add/
    ```

  - 然后`java`代码中使用注解注入：

    ```java
        private static  String addPlayerUrl;
        private static  String removePlayerUrl;
        public static String addBotUrl;
    
        @Value("${botrunning.url.add}")
        public void setddBotUrl(String url) {
            WebSocketServer.addBotUrl = url;
        }
    
        @Value("${matching.url.add}")
        public void setAddPlayerUrl(String url) {
            WebSocketServer.addPlayerUrl = url;
        }
    
        @Value("${matching.url.remove}")
        public void setRemovePlayerUrl(String url) {
            WebSocketServer.removePlayerUrl = url;
        }
    ```

    对于`static`变量必须通过`set`接口设置，因为`@Value`只能注入对象实例，不能直接注入类本身。

- 该项目中的`docker`相关命令：

  ```bash
  # 在项目根目录下，根据配置文件，在后台运行整套容器服务
  docker compose up -d
  
  docker compose down
  
  docker compose restart
  ```

  - `docker compose`允许用户通过一个`yaml`文件定义和运行多个容器。
  - `up`执行：读取配置、拉取构建镜像、创建容器、启动服务。
  - `-d`分离模式，后台运行

#### ubuntu中的nginx主流部署方式

- 文件目录

```bash
vim /etc/nginx/site-available/project_name
```

- 在available目录下创建项目的nginx文件：

```ini
server {
    listen 80;
    server_name kob.jaxenwang.top;
    location / {
        root /home/jax/kob/web;
        index index.html;
        try_files $uri  $uri/ /index.html;
    }
    location /api {
        proxy_pass http://127.0.0.1:8080;
        # 传递真实的客户端信息，否则后端看到的 IP 全是 127.0.0.1
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    location /websocket {
        proxy_pass http://127.0.0.1:8080;
        # 显式指定使用http/1.1
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "Upgrade";

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

#### 如何申请免费的SSL证书

使用`certbot`一个`python`脚本工具申请免费证书。

```bash
# 下载certbot
apt install certbot python3-certbot-nginx

# 获取并安装证书，certbot会自动扫描nginx配置，找到域名，并申请证书、修改配置文件
certbot --nginx
```

如果只要个证书文件，不想自动修改配置文件：

```bash
certbot certonly --nginx

# 查看自动续期是否正常，默认90天 
certbot renew --dry-run
```

生成的证书文件在`/etc/letsencrypt/live/域名/`

