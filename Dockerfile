# Use uma imagem base do OpenJDK
FROM openjdk:8-jdk-slim

# Defina variáveis de ambiente para Scala e SBT
ENV SCALA_VERSION=2.12.15
ENV SBT_VERSION=1.2.8

# Instale dependências necessárias
RUN apt-get update && apt-get install -y \
    curl \
    unzip \
    && rm -rf /var/lib/apt/lists/*

