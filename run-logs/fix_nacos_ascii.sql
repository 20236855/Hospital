USE `ry-config`;
UPDATE config_info SET content='file:
  domain: http://127.0.0.1:9300
  path: D:/ruoyi/uploadPath
  prefix: /statics
fdfs:
  domain: http://127.0.0.1
  soTimeout: 3000
  connectTimeout: 2000
  trackerList: 127.0.0.1:22122
minio:
  url: http://127.0.0.1:9000
  accessKey: minioadmin
  secretKey: minioadmin
  bucketName: test
referer:
  enabled: false
  allowed-domains: localhost,127.0.0.1,ruoyi.vip,www.ruoyi.vip', type='yaml' WHERE data_id='ruoyi-file-dev.yml';
UPDATE config_info SET content='spring:
  autoconfigure:
    exclude: com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceAutoConfigure
feign:
  sentinel:
    enabled: true
  okhttp:
    enabled: true
  httpclient:
    enabled: false
  client:
    config:
      default:
        connectTimeout: 10000
        readTimeout: 10000
  compression:
    request:
      enabled: true
      min-request-size: 8192
    response:
      enabled: true
management:
  endpoints:
    web:
      exposure:
        include: ''*''', type='yaml' WHERE data_id='application-dev.yml';
UPDATE config_info SET content='spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
  datasource:
    druid:
      stat-view-servlet:
        enabled: true
        loginUsername: ruoyi
        loginPassword: 123456
    dynamic:
      druid:
        initial-size: 5
        min-idle: 5
        maxActive: 20
        maxWait: 60000
        connectTimeout: 30000
        socketTimeout: 60000
        timeBetweenEvictionRunsMillis: 60000
        minEvictableIdleTimeMillis: 300000
        validationQuery: SELECT 1 FROM DUAL
        testWhileIdle: true
        testOnBorrow: false
        testOnReturn: false
        poolPreparedStatements: true
        maxPoolPreparedStatementPerConnectionSize: 20
        filters: stat,slf4j
        connectionProperties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000
      datasource:
        master:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://localhost:3306/ry-cloud?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8
          username: root
          password: root
mybatis:
  typeAliasesPackage: com.ruoyi.system
  mapperLocations: classpath:mapper/**/*.xml
springdoc:
  gatewayUrl: http://localhost:8080/${spring.application.name}
  api-docs:
    enabled: true', type='yaml' WHERE data_id='ruoyi-system-dev.yml';
UPDATE config_info SET content='spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ry-cloud?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8
    username: root
    password: root
mybatis:
  typeAliasesPackage: com.ruoyi.gen.domain
  mapperLocations: classpath:mapper/**/*.xml
springdoc:
  gatewayUrl: http://localhost:8080/${spring.application.name}
  api-docs:
    enabled: true
gen:
  author: ruoyi
  packageName: com.ruoyi.system
  autoRemovePre: false
  tablePrefix: sys_
  allowOverwrite: false', type='yaml' WHERE data_id='ruoyi-gen-dev.yml';
UPDATE config_info SET content='spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:', type='yaml' WHERE data_id='ruoyi-auth-dev.yml';
UPDATE config_info SET content='spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
  cloud:
    gateway:
      server:
        webflux:
          discovery:
            locator:
              lowerCaseServiceId: true
              enabled: true
          routes:
            - id: ruoyi-auth
              uri: lb://ruoyi-auth
              predicates:
                - Path=/auth/**
              filters:
                - name: CacheRequestBody
                  args:
                    bodyClass: java.lang.String
                - ValidateCodeFilter
                - StripPrefix=1
            - id: ruoyi-gen
              uri: lb://ruoyi-gen
              predicates:
                - Path=/code/**
              filters:
                - StripPrefix=1
            - id: ruoyi-job
              uri: lb://ruoyi-job
              predicates:
                - Path=/schedule/**
              filters:
                - StripPrefix=1
            - id: ruoyi-system
              uri: lb://ruoyi-system
              predicates:
                - Path=/system/**
              filters:
                - StripPrefix=1
            - id: ruoyi-file
              uri: lb://ruoyi-file
              predicates:
                - Path=/file/**
              filters:
                - StripPrefix=1
security:
  captcha:
    enabled: true
    type: math
  xss:
    enabled: true
    excludeUrls:
      - /system/notice
  ignore:
    whites:
      - /auth/logout
      - /auth/login
      - /auth/register
      - /*/v2/api-docs
      - /*/v3/api-docs
      - /csrf
springdoc:
  webjars:
    prefix:', type='yaml' WHERE data_id='ruoyi-gateway-dev.yml';
UPDATE config_info SET content='spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ry-cloud?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8
    username: root
    password: root
mybatis:
  typeAliasesPackage: com.ruoyi.job.domain
  mapperLocations: classpath:mapper/**/*.xml
springdoc:
  gatewayUrl: http://localhost:8080/${spring.application.name}
  api-docs:
    enabled: true', type='yaml' WHERE data_id='ruoyi-job-dev.yml';
