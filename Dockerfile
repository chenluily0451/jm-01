FROM ubuntu-1404

MAINTAINER hary <94093146@qq.com>

ADD target/aegis-jm.jar  /app/aegis-jm/lib/

RUN mkdir /app/aegis-jm/logs

RUN mkdir /app/aegis-jm/config

WORKDIR /app/aegis-jm

# 配置可被挂在
VOLUME /app/aegis-jm/config
VOLUME /app/aegsi-jm/logs

EXPOSE 8080/tcp

CMD  java -jar lib/aegis-jm.jar

