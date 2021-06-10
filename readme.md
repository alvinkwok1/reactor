# reactor模型示例代码

## 简介
该项目是我在学习netty过程中所产生的，主要包含两个部分，一个部分是netty的简单实现，一个是reactor模型单线程版本的demo代码。
参考文章：https://alvinkwok.cn/2021/06/06/design_pattern/reactor%E6%A8%A1%E5%9E%8B/

## 组成结构
- 在bootstrap和core下面是一份简单的netty实现
  内部实现了reactor模型，CloseFuture，Promise功能
- 在example下面是一份reactor模型的实现，该模型实现了Douglas C. Schmidt的《Reactor An Object Behavioral Pattern for Demultiplexing and Dispatching Handles for Synchronous Events》中的LoggerServer

## 项目编译
```shell
mvn package
```
将会在target目下生成一个reactor-1.0.jar的文件
## Simple Netty服务端启动
```shell
java -cp target/reactor-1.0.jar cn.alvinkwok.bootstrap.Server
```
## Reactor Demo启动
```shell
java -cp target\reactor-1.0.jar cn.alvinkwok.example.ExampleServer
```

验证可以使用telnet来做快速测试
