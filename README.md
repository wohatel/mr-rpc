## 1.使用要求
```
java版本:17
```

## 2.rpc的坐标依赖
```
1.通过源码将该项目通过mvn安装到本地的依赖为:
<dependency>
    <groupId>io.github.wohatel</groupId>
    <artifactId>mr-rpc</artifactId>
    <version>1.0.0</version>
</dependency>
后续会上传到mvn中央仓库或私服
```

## 3.服务端模块
```
1. 一个项目如果一个项目服务想要对外提供rpc服务
  server端,项目一般会有两个模块
  api模块 : 只定义具体的接口
  server模块 : 实现接口后具体的实现
```

## 4.服务端api模块引入对应依赖


## 5.服务端api模块需要提供的服务接口打注解
```
1. 那么api模块引入上述依赖,api的接口上打上如下注解@RpcClient(url = "***"),其中url为服务的http://ip:port,比如http://127.0.0.1:9090:
   
  @RpcClient(url = "${seabox.data.user}")
  public interface FileApi {
    // 获取文件信息
    FileInfo getFile(Long fileId);
    // 流下载,返回值必须为inputstream
    InputStream getFileStream(Long fileId);
    // 上传,方法中必须有一个参数包含inputstream
    int upload(String name, InputStream input);
  }

```

## 6.服务端server模块引入对应依赖
```
  1. server模块依赖api并实现
    <dependency>
        <groupId>com.server.api</groupId>
        <artifactId>api</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
```

## 7.服务端实现该fileApi接口
```
4. 实现该api的接口,注意该实现类必须交由spring管理,也就是说需要打上 @component 或者@service ...等注解
@Service
public class FileApiService implements FileApi {

    @Override
    public FileInfo getFile(Long streamId) {
        FileInfo info = new FileInfo();
        info.setName("wenjian");
        return info;
    }

    @MrVersion(1)
    public FileInfo getFile$1(Long streamId) {
        FileInfo info = new FileInfo();
        info.setName("李四");
        return info;
    }

    @SneakyThrows
    @Override
    public InputStream getStream(Long stareamId) {
        return new FileInputStream("/Users/Desktop/abc.jar");
    }

    @Override
    @SneakyThrows
    public int upload(String str, InputStream input) {
        StreamUtil.inputStreamToOutputStream(input, new FileOutputStream("/tmp/abc"));
        FileInfo info = new FileInfo();
        info.setName("wenjian1");
        return 0;
    }
}

```

## 8.在server的启动类上加上注解: @EnableMrRpc 并启动
```
@SpringBootApplication
@EnableMrRpc
public class AppMain {

    public static void main(String[] args) {
        SpringApplication.run(AppMain.class, args);
    }

}
```


----------------------------------------

以下是client端调用

----------------------------------------

## 9.另外一个服务想要远程调用FileApi 服务,需要引入依赖
```
  1. 服务引入依赖
    <dependency>
        <groupId>com.server.api</groupId>
        <artifactId>api</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
  
```

## 10.配置文件中需要配置url
    例如 @RpcClient(url = "${seabox.data.user}"),则需要在项目properties文件中配置如下(或yml文件)
```
seabox.data.user=http://127.0.0.1:9090
```

## 11.client端项目启动类上同样需要打打上注解 @EnableMrRpc
```
@SpringBootApplication
@EnableMrRpc
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }
}
```


## 12. client端远程调用的注入和调用示例
       NAppRunner : 示例类,类需要交由spring管理,需要打上注解 @Component 或@Service ....
       FileApi: 远端服务提供的api, 需要打上注解 @MrAutowired 才能调用远程接口
```
@Component
public class NAppRunner{

    @MrAutowired
    FileApi fileApi;


    public void test() throws Exception {
        // 文件上传
        int upload = fileApi.upload("xiaozi", new FileInputStream("/tmp/使用手册.docx"));
        
        // 文件下载
        InputStream inputStream = fileApi.getStream(123l);
        
        // 文件详情
        FileInfo fileInfo= getFile(123l)

    }
}
```


----------------------------------------

以下是是简单的调用总结

----------------------------------------

## 13. 简单实用总结

```
1: api引入rpc的依赖,对外提供的接口类打上@RpcClient 注意url可配置也可写死固定路径 http://ip:port
2: server端和client端都需要引入api依赖
3: server端和client端启动类都需要打上@EnableMrRpc注解
4: client端使用时,需要 @MrAutowired 将这个远程调用类注入
5: 如果url类似${abc.def.dd} 客户端,还需要在项目配置文件中配置  abc.def.dd=http://*****
```


----------------------------------------

以下是进阶配置

----------------------------------------

## 14. 版本支持

```
客户端注入

@MrAutowired(version = 10)
FileApi fileApi;

表示客户端将要调用服务端提供的version最大为10: 什么意思? 请继续看,注意看server端实现类方法的'注解'和'方法名'
1.以下示例有同一个服务getFile有三个方法,只有getFile本身是来自FileApi的视线,其它两个打上@MrVersion注解的是 FileApiService实现的方法
2.打上@MrVersion注解的方法有要求,方法名必须满足--"原方法名$版本号"的格式
3.示例1
@MrAutowired(version = 10)
FileApi fileApi;
客户端注入后,调用 fileApi.getFile后,服务端会找打标 MrVersion(10)的方法,并且方法名为 getFile$10, 如果找不到就会去找 getFile$小于10的方法
4.示例2
@MrAutowired(version = 12)
FileApi fileApi;
客户端注入后,调用 fileApi.getFile后,服务端会找打标 MrVersion(12)的方法,并且方法名为 getFile$12, 显然没找到, 但是找到了 MrVersion(15):getFile$15  MrVersion(10):getFile$10 以及 getFile本身
那么取MrVersion(10):getFile$10这个版本调用,因为MrVersion(10):getFile$10 小于version12 ,但10离12最近
5.示例3
@MrAutowired
FileApi fileApi;
如果不填写version,那么就会找最大的也就是 MrVersion(15):getFile$15方法

@Service
public class FileApiService implements FileApi {

    @Override
    public FileInfo getFile(Long streamId) {
        FileInfo info = new FileInfo();
        info.setName("wenjian");
        return info;
    }

    // 服务端打上 @MrVersion(10) 表示这个服务版本为 10 ,方法名必须为 getFile$10
    @MrVersion(10)
    public FileInfo getFile$10(Long streamId) {
        FileInfo info = new FileInfo();
        info.setName("李四");
        return info;
    }

    // 服务端打上 @MrVersion(15) 表示这个服务版本为 15 ,方法名必须为 getFile$15
    @MrVersion(15)
    public FileInfo getFile$15(Long streamId) {
        FileInfo info = new FileInfo();
        info.setName("张三");
        return info;
    }

}

```

## 14. client 发送时添加header拦截配置

```
1. 需定义一个 MrRequestInterceptor

有时候,我们想在客户调用远程服务前,传输一些header,此时需要实现一个接口,并交由spring管理
如下

    @Bean
    public MrRequestInterceptor mrRpcInterceptor() {
        // 定义一个MrInterceptor, 可以由客户端提供具体逻辑
        
    }

```


## 15. server 想要接受到请求前和请求后处理逻辑

```
1. server端需要实现一个类 MrServerAroundAdvice交由spring管理
    如下,是服务端接受到请求后,可以获取cookie,或者请求头,一般可以用作鉴权

    @Bean
    public MrServerAroundAdvice  mrServerAroundAdvice(){
        ......
    }

```

```
连接池配置优化


```
    链接超时时间-发送请求后超时
    mr.rpc.rest.connectTimeout = 5000;
    连接池最大数量
    mr.rpc.rest.max-pool-size=100
    从连接池中获取链接最大等待时间
    mr.rpc.rest.connection-request-timeout=5000;
    读取或写入操作时的超时时间
    mr.rpc.rest.socket-timeout=5000;
    每个目标主机最大连接数
    mr.rpc.rest.max-per-route-size=20;

-----------------
结束

