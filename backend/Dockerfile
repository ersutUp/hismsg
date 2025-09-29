# 构建阶段（使用Maven镜像）
FROM maven:3.8.6-openjdk-18 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# 构建并跳过测试
RUN mvn package -DskipTests


# 参考： https://spring.io/guides/topicals/spring-boot-docker/
FROM eclipse-temurin:17-jdk
VOLUME /opt/yml
VOLUME /opt/data
ENV projname=hismsg
RUN mkdir /opt/${projname}/ && mkdir /opt/yml

# 关于时区：https://wsgzao.github.io/post/docker-localtime/
# 添加时区环境变量，亚洲，上海
ENV TimeZone=Asia/Shanghai
# 使用软连接，并且将时区配置覆盖 / etc/timezone
RUN ln -snf /usr/share/zoneinfo/$TimeZone /etc/localtime && echo $TimeZone > /etc/timezone

#安装额外的辅助工具（可选）
#RUN microdnf update && microdnf install sudo iputils net-tools hostname findutils less nano && microdnf clean all
COPY --from=build /app/target/${projname}-*.jar /opt/${projname}/app.jar
#配置文件
COPY src/main/resources/application*.yml /opt/yml/
WORKDIR /opt/${projname}/
ENTRYPOINT ["sh", "-c", "java  ${JAVA_OPTS}  -jar /opt/${projname}/app.jar --spring.config.location=/opt/yml/ --spring.profiles.active=prod ${SB_OPTS} "]
# 如果有可选参数值，可混用CMD.  https://zhuanlan.zhihu.com/p/30555962
# CMD [""]