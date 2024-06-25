```shell
docker run -d -p 8080:8080 \
--restart='always' \
-e JAVA_OPTS='-Xms2048m -Xmx2048m -Xmn1024m' \
-e "SPRING_PROFILES_ACTIVE=home" \
--name yibot yibot:0.0.2
```