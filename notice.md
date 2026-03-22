但是！网关依赖 Common 有一个巨大的“坑”
请务必注意： 由于 Spring Cloud Gateway 是基于 WebFlux（异步非阻塞） 的，而 common 模块以后可能会引入 MyBatis-Plus 或 spring-boot-starter-web（Servlet 阻塞式）。

[!CAUTION]
冲突预警：如果在网关的类路径里同时出现了 webflux 和 web (Servlet)，网关会启动报错或路由失效。

1. 检查 nexusmall-common 的 pom.xml
   确保 common 模块里不要直接放入 spring-boot-starter-web。业务模块（如 Product）需要 Web 功能时，自己在各自的 pom.xml 里加。

2. 在 nexusmall-gateway 中引入
   XML
   <dependency>
   <groupId>com.yourname.nexusmall</groupId>
   <artifactId>nexusmall-common</artifactId>
   <version>0.0.1-SNAPSHOT</version>
   <exclusions>
   <exclusion>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-web</artifactId>
   </exclusion>
   </exclusions>
   </dependency>