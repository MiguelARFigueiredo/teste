# Use a imagem base do OpenJDK
FROM openjdk:8-jdk-alpine

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia os arquivos compilados do SBT para o container
COPY target/universal/stage .

# Expõe a porta que a aplicação irá rodar
EXPOSE 9000

# Comando para executar a aplicação
CMD ["bin/play-java-hello-world-tutorial-2-6-x", "-Dplay.http.port=9000"]
