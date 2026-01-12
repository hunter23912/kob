# `SpringBoot`框架学习与`Java`项目实战

## `JAVA`学习流程

### `idea`的一些操作

- 打开设置： `ctrl + alt + s`

### `java`语法

#### 变量

- 常量：用`final`修饰。
- 变量类型：`byte`1字节；`short`2字节

#### 输入输出

- `Scanner`效率较低，输入规模较小时使用
- `BufferedReader`和`InputStreamReader`规模较大时使用，要抛异常
- `System.out.print`和`System.out.println`
- `BufferedWriter`和`OutputStreamWriter`

#### 数组

- 定义方式：`int[] a = new int[10] `

#### 常用`API`

- `length`返回数组长度
- `Arrays.sort()`数组排序
- `Arrays.fill(int[] a, int val)`填充数组
- `Arrays.toString()`数组转字符串
- `Arrays.deepToString()`多维数组转为字符串

#### 字符串操作

- `String.format`字符串格式化
- `equals()`判断两个字符串是否相等
- `StringBuffer`线程安全，速度较慢，`StirngBuilder`线程不安全，速度较快。

#### 接口

定义类中所需包含的函数，有点像基类和虚类，用来规范某些类必须实现的方法。

#### 注解与反射

- 注解`@`，给类、方法、字段等代码贴上标签，自身不会主动执行任何逻辑；
- 反射：运行时动态获取类的信息，并操作类的属性和方法。

## `java`项目：`King of Bot`

### `MVC`

- `C`: `controller`，类似于`django`中的`view`
- `V`: `view`，前端页面,类似于`django`中的`template`
- `M`: `model`，数据库解构
