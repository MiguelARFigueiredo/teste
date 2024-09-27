# Use uma imagem base do OpenJDK
FROM openjdk:8-jdk-slim

# Defina o diretório de trabalho
WORKDIR /app

# Copie o arquivo JAR para o contêiner
COPY target/my-app.jar /app/my-app.jar

# Expor a porta da aplicação
EXPOSE 9000

# Comando para rodar a aplicação
CMD ["java", "-jar", "/app/my-app.jar"]
