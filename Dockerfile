# Multi-stage build
FROM maven:3.8-openjdk-11 AS backend-build
WORKDIR /app
COPY pom.xml .
COPY *.java .
RUN mvn clean compile

FROM openjdk:11-jre-slim AS backend
WORKDIR /app
COPY --from=backend-build /app/target/classes ./classes
COPY --from=backend-build /app/target/dependency ./lib
COPY *.java .
COPY pom.xml .

FROM nginx:alpine AS frontend
COPY *.html *.css *.js *.csv *.xlsx /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/conf.d/default.conf

FROM openjdk:11-jre-slim
WORKDIR /app

# Install nginx
RUN apt-get update && apt-get install -y nginx && rm -rf /var/lib/apt/lists/*

# Copy backend
COPY *.java .
COPY pom.xml .
COPY --from=maven:3.8-openjdk-11 /usr/share/maven /usr/share/maven
RUN ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

# Copy frontend
COPY *.html *.css *.js *.csv *.xlsx /var/www/html/
COPY nginx-app.conf /etc/nginx/sites-available/default

# Startup script
COPY start.sh .
RUN chmod +x start.sh

EXPOSE 8080 80
CMD ["./start.sh"]