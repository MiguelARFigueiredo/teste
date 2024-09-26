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

# Baixe e instale o Scala
RUN mkdir -p /usr/share/scala \
    && curl -fsL "https://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz" | tar -xz -C /usr/share/scala --strip-components=1

# Baixe e instale o SBT
RUN mkdir -p /usr/share/sbt \
    && curl -fsL "https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz" | tar -xz -C /usr/share/sbt --strip-components=1

# Adicione SBT e Scala ao PATH
ENV PATH="/usr/share/scala/bin:/usr/share/sbt/bin:${PATH}"

# Confirme a instalação
RUN scala -version \
    && sbt sbtVersion

# Defina o diretório de trabalho para o projeto
WORKDIR /play-java-hello-world-tutorial-2.6.x

# Copie o código fonte da sua aplicação para o diretório de trabalho
COPY . .

# Compile e construa o projeto com o SBT
RUN sbt clean compile stage

# Diretório do executável após a construção
ENV EXECUTABLE_DIR=/play-java-hello-world-tutorial-2.6.x/target/universal/stage/bin
ENV EXECUTABLE_FILE=play-java-hello-world-tutorial-2-6-x

# Defina as variáveis de ambiente para a conexão com o banco de dados
ENV DB_DRIVER=com.mysql.cj.jdbc.Driver
ENV DB_URL="jdbc:mysql://127.0.0.1:3306/playdb?useSSL=false"
ENV DB_USERNAME=${MY_USERNAME}
ENV DB_PASSWORD=${MY_PASSWORD}

# Comando para iniciar a aplicação e garantir a remoção do RUNNING_PID
CMD ["sh", "-c", "rm -f /play-java-hello-world-tutorial-2.6.x/target/universal/stage/RUNNING_PID && $EXECUTABLE_DIR/$EXECUTABLE_FILE"]
