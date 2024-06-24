############################################################################################################
#####构建命令
############################################################################################################

echo $JAVA_HOME
# 项目名称, 一般也是项目路径
PROJECT=yibot
# 组 ID
GROUP_ID=com.yixihan


##############################打包###########################################
# 跳转到源代码目录, 执行 gradle 命令 (打包) (需更改 gradle 路径)
echo "=============gradle 打包============="


cd /var/jenkins_home/workspace/${PROJECT}
chmod 777 ./gradlew
./gradlew clean bootJar

############################################################################

#############################删除原有镜像,容器###############################
TAG=`date "+%Y%m%d"`
IMAGE_NAME=$PROJECT

echo "=============删除容器&镜像============="
#镜像id
iid=$(docker images | grep $IMAGE_NAME | awk '{print $3}')
#容器id
cid=$(docker ps -a | grep $IMAGE_NAME | awk '{print $1}')


# 删除容器
if [ -n "$cid" ]; then
  echo "存在容器$IMAGE_NAME，cid=$cid,删除容器。。。"
  docker rm -f $cid
else
   echo "不存在$IMAGE_NAME容器"
fi

# 删除镜像
if [ -n "$iid" ]; then
  echo "存在镜像$IMAGE_NAME，iid=$iid,删除容器镜像。。。"
  docker rmi -f $iid
else
   echo "不存在$IMAGE_NAME镜像"
fi

############################################################################

###############################构建镜像,运行容器#############################

# 跳转到 target 目录
cd /var/jenkins_home/workspace/${PROJECT}

# 构建镜像
echo "=============构建镜像$IMAGE_NAME============="
docker build -t ${PROJECT}:$TAG .

# 启动容器
echo ""=============启动容器$IMAGE_NAME"============="

docker run -p 27692:8080 -d --restart='always' \
-e JAVA_OPTS='-Xms2048m -Xmx2048m -Xmn1024m'  \
-e "SPRING_PROFILES_ACTIVE=home" \
--name $IMAGE_NAME $IMAGE_NAME:$TAG
############################################################################
#
docker run -p 8080:8080 -d --restart='always' \
-e JAVA_OPTS='-Xms2048m -Xmx2048m -Xmn1024m'  \
-e "SPRING_PROFILES_ACTIVE=home" \
--name yibot yibot:0.0.1