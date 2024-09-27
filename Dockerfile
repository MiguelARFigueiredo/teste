# Use uma imagem base do OpenJDK
FROM openjdk:8-jdk-slim

# Comando para manter o contêiner rodando
CMD ["java", "-version"]
