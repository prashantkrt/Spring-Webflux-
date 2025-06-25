FROM openjdk:21
WORKDIR /app
COPY  target/soe-product-service.jar soe-product-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "soe-product-service.jar"]
CMD ["--server.port=8080"]

# docker build -t soe-product-service:v1 .
# docker run -p 8080:8080 soe-product-service:v1