# 开发网站用到的一些知识

### 快速建立数据库`mysql`

####`ubuntu`下载数据库：
```bash
sudo apt-get update
sudo apt-get install mysql-server -y
```

#### 修改监听端口
```bash
sudo vim /etc/mysql/mysql.conf.d/mysqld.cnf
```
将`bind-address`的值改为`0.0.0.0`

#### 进入`mysql`创建用户和密码：允许外部用户连接
```bash
# 允许从任何 IP 连接（% 是通配符），用密码 'wjh123456' 
ALTER USER 'root'@'%' IDENTIFIED WITH caching_sha2_password BY 'wjh123456';

#  给所有权限
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;

# 刷新权限
FLUSH PRIVILEGES;
```

### 配置`SpringBoot`
- 在`pom.xml`中添加依赖：
  - `Spring Boot Starter JDBC`
    - `java`官方提供的数据库连接标准API，偏低层。
    - `Spring Data JPA`支持自动生成SQL，对象映射关系(ORM)
  - `Project Lombok`
    - 通过注解自动生成样板代码。
  - `MySQL Connector/Java`
    - `MySQL`数据库的`JDBC`驱动。
  - `mybatis-plus-boot-starter`
    - `MyBatis-Plus`是`MyBatis`的增强工具，在`MyBatis`的基础上只做增强不做改变，为简化开发、提高效率而生。
  - `mybatis-plus-generator`
    - `MyBatis-Plus`代码生成器，可以根据数据库表结构生成实体类、Mapper接口、Mapper XML文件等代码，极大地提高了开发效率。

  > 下面两个先别装
  - `spring-boot-starter-security`
    - 提供了强大的身份验证和授权功能，帮助开发者轻松实现安全控制。
  - `jjwt-api`
    - 用于创建和验证`JWT`（JSON Web Tokens）的库，常用于用户认证和授权。
  
### `SprintBoot`常见层级模块
- `pojo`层：存放实体类，对应数据库表结构。
- `mapper`层：将`pojo`层的`class`中的操作，映射成`SQL`语句。
- `service`层：写具体的业务逻辑，组合使用`mapper`层中的操作。
- `controller`层：处理HTTP请求，接收用户输入，调用服务层逻辑，并返回响应。
- 常用组织方式
  - 通常在`controller`层中处理请求和响应调用`service`接口；
  - 在`service`层中写接口，在其子目录`impl`下写接口的实现；
  - 在`mapper`层中处理数据库操作，在`pojo`层中定义数据结构。