# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.0.4/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.0.4/maven-plugin/build-image.html)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the
parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.
模块名称	建议端口	说明
nexusmall-gateway	88	网关作为外网唯一入口，越短越好（或用 80）
nexusmall-auth	8000	认证中心，独立于业务之外
nexusmall-product	10000	商品服务（核心业务 1 号）
nexusmall-order	11000	订单服务（核心业务 2 号）
nexusmall-cart	12000	购物车服务
nexusmall-ware	13000	仓储服务
nexusmall-user	14000	用户服务
nexusmall-coupon	15000	优惠券/营销服务
nexusmall-search	16000	搜索服务
nexusmall-third-party	17000	第三方服务（OSS、短信等） 
