FROM eclipse-temurin:25_36-jdk-alpine-3.21
WORKDIR /app
COPY ./target/Shirtso*.jar ./shirtso.jar
COPY ./docker/application.yml ./application.yml
EXPOSE 8080
CMD ["java", "-jar", "./shirtso.jar", "--spring.config.location=file:./application.yml"]