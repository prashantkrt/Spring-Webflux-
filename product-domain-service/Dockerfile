FROM openjdk:21
WORKDIR /app
COPY target/product-domain-service.jar product-domain-service.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "product-domain-service.jar"]
CMD ["--server.port=8082"]

# docker build -t product-domain-service:v1 .
# docker run -p 8082:8082 product-domain-service:v1