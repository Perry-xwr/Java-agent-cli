# Java Agent CLI

## 项目简介

Java Agent CLI 是一个基于 Java 17 实现的轻量级 ReAct Agent 框架。项目将大语言模型的推理能力与本地工具执行结合起来，支持 LLM 调用、Function Calling、Tool Registry、ReAct Loop 和文件分析工具。

Agent 会维护对话历史，将用户请求交给 GLM 推理；当模型判断需要读取或搜索项目文件时，Agent 会执行对应工具，并将工具结果作为 observation 返回给模型，最终生成回答。

## Features

- GLM API Integration
- ReAct Agent
- Function Calling / Tool Calling
- Tool Registry
- Multi-tool Execution
- Tool Failure Recovery
- Conversation History and `clear`
- File Analysis Tools

## Architecture

```text
User
  |
  v
Agent <-------------------------+
  |                             |
  v                             |
LLMClient                       |
  |                             |
  v                             |
GLM -- tool_call --> ToolRegistry
                         |
                         v
                       Tools
                         |
                         +---- observation ----+
```

- **Agent** 负责保存消息历史、控制 ReAct 循环并决定何时返回最终回答。
- **LLM / GLM** 负责理解用户意图、推理并生成工具调用或最终回答。
- **ToolRegistry** 负责注册、描述、查找和执行工具。
- **Tools** 负责执行具体任务，例如列出、读取和搜索文件。

## Supported Tools

| Tool | Description |
| --- | --- |
| `list_files` | 递归列出工作目录中的普通文件 |
| `read_file` | 读取 UTF-8 文本文件 |
| `search_code` | 递归搜索包含指定关键字的代码行 |

## Demo

用户输入：

```text
读取README.md
```

执行流程：

```text
Agent
  ↓
GLM 判断需要工具
  ↓
调用 read_file
  ↓
返回文件内容 observation
  ↓
GLM 生成最终回答
```

输入 `clear` 可以清空当前对话历史；system message 会被保留。

## Run

运行测试：

```shell
mvn test
```

启动 CLI：

```shell
mvn exec:java '-Dexec.mainClass=com.agent.Main'
```

启动后可以输入普通问题或文件任务，例如：

```text
介绍一下Java17
读取README.md
clear
```

## Environment

- Java 17
- Maven 3.9+
- 环境变量 `GLM_API_KEY`
- 本地 HTTP 代理 `127.0.0.1:7897`

PowerShell 当前会话设置示例：

```powershell
$env:GLM_API_KEY = "your-api-key"
```

API Key 不应写入源码、README、`.env` 提交或其他 Git 跟踪文件。项目的 `.gitignore` 已忽略 `.env`。

如需显示 GLM HTTP 状态码，可启用调试输出：

```powershell
$env:GLM_DEBUG = "true"
```

默认不会打印 HTTP 状态码。

## Project Structure

```text
src/main/java/com/agent/
├── Main.java                 # CLI 入口
├── agent/
│   └── Agent.java            # ReAct 循环与消息历史
├── llm/
│   ├── LLMClient.java        # LLM 抽象接口
│   ├── GlmClient.java        # GLM API 客户端
│   ├── Message.java          # 对话消息
│   ├── LLMResponse.java      # 模型响应
│   ├── ToolCall.java         # 工具调用
│   └── ToolDefinition.java   # 工具 Schema
└── tool/
    ├── Tool.java             # 统一工具接口
    ├── ToolRegistry.java     # 工具注册与执行
    ├── ListFilesTool.java
    ├── ReadFileTool.java
    └── SearchCodeTool.java
```
