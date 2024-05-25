FROM openjdk:17
EXPOSE 27692

LABEL authors="yixihan"
MAINTAINER yixihan<yixihan20010617@gmail.com>

ENV TZ 'Asia/Shanghai'
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US.en
ENV LC_ALL en_US.UTF-8

VOLUME /tmp
ADD target/*.jar  /app.jar
RUN bash -c 'touch /app.jar' cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
ENTRYPOINT ["java","-jar","/app.jar"]