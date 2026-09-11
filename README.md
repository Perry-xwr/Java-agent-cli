# agent-cli

一个基于 Java 17 和 Maven 的最小命令行项目。程序逐行读取标准输入，并由 `Agent` 生成响应；输入 `hello` 时返回 `Hello, Agent!`。

## Environment

- Java 17
- Maven 3.9+

## Build & Test

```shell
mvn test
```

## Compile

```shell
mvn -q -DskipTests compile
```

## Run

先完成编译，然后运行：

```shell
java -cp target/classes com.agent.Main
```

输入示例：

```text
hello
```

输出：

```text
Hello, Agent!
```
