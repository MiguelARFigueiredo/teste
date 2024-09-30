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

# Comando para manter o container ativo
CMD ["tail", "-f", "/dev/null"]
