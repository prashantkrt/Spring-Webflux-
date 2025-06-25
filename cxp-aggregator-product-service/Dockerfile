FROM openjdk:21
WORKDIR /app
COPY  target/product-aggregator-service.jar product-aggregator-service.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "product-aggregator-service.jar"]
CMD ["--server.port=8081"]

# docker build -t product-aggregator-service:v1 .
# docker run -p 8081:8081 product-aggregator-service:v1