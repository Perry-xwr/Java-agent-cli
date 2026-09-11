# AI 辅助开发记录

## Prompt

本次开发通过多轮提示完成，主要要求包括：

- 创建 Java 17 Maven CLI 项目，并配置 `com.agent:agent-cli:0.1.0`。
- 实现 `Agent.respond(String input)` 的问候与普通输入响应逻辑。
- 实现只负责标准输入、输出的 `Main` 程序入口。
- 使用 JUnit 5.10.2 测试 `hello`、`hi`、空白、大小写、首尾空格和 `null` 等情况。
- 补充项目忽略规则、README 和 AI 辅助开发记录，并完成构建及 CLI 验证。

## AI 做了什么

- 创建了标准 Maven 源码和测试目录以及 `pom.xml`。
- 配置 Java release 17 和 test scope 的 JUnit Jupiter 5.10.2。
- 实现了 `Agent` 业务逻辑与 `Main` 控制台 I/O。
- 编写并完善了 `AgentTest` 的正常输入和边界测试。
- 创建 `.gitignore`、`README.md` 和本记录文件。
- 运行 Maven 测试、编译和命令行输入输出验证。

## 我应该人工检查什么

- 检查 `Agent.respond` 的返回文案和空白处理是否符合 Lab 要求。
- 检查 `Main` 是否只负责 I/O，并确认退出交互程序的方式符合当前终端习惯。
- 检查 `pom.xml` 的项目坐标、Java 版本和 JUnit 版本。
- 检查测试名称与覆盖场景是否清晰。
- 在提交前查看 `git status` 和文件差异，确认没有不需要的文件。

本记录没有声称用户进行了未发生的人工代码修改。

## 如何验证

运行全部测试：

```shell
mvn test
```

仅编译主代码：

```shell
mvn -q -DskipTests compile
```

编译完成后启动 CLI：

```shell
java -cp target/classes com.agent.Main
```

输入 `hello` 后应输出：

```text
Hello, Agent!
```

也可以通过管道进行一次性验证。在 PowerShell 中执行：

```powershell
"hello" | java -cp target/classes com.agent.Main
```
