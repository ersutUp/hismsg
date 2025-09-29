# 构建阶段（使用node镜像）
FROM node:22.20.0-alpine AS build
WORKDIR /app
COPY ./ ./
# 构建并跳过测试
RUN npm install && npm run build

FROM nginx:1.29.1

RUN mkdir /opt/vue

# 关于时区：https://wsgzao.github.io/post/docker-localtime/
# 添加时区环境变量，亚洲，上海
ENV TimeZone=Asia/Shanghai
# 使用软连接，并且将时区配置覆盖 / etc/timezone
RUN ln -snf /usr/share/zoneinfo/$TimeZone /etc/localtime && echo $TimeZone > /etc/timezone

COPY --from=build /app/dist/ /opt/vue/
COPY nginx/nginx.conf /etc/nginx/nginx.conf