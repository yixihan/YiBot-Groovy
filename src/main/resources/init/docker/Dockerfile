FROM openjdk:17
EXPOSE 28080

LABEL authors="yixihan"
MAINTAINER yixihan<yixihan20010617@gmail.com>

VOLUME /tmp
ENV LANG zh_CN.UTF-8
ADD target/*.jar  /app.jar
RUN bash -c 'touch /app.jar' cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
ENTRYPOINT ["java","-jar","/app.jar"]