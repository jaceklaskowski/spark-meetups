# spark-docker-example

The project uses [Docker Plugin](https://www.scala-sbt.org/sbt-native-packager/formats/docker.html#docker-plugin) from [sbt-native-packager](https://www.scala-sbt.org/sbt-native-packager/index.html) plugin.

```text
sbt 'show dockerCommands'
```

```text
sbt clean docker:stage
```

Review `target/docker/stage` directory (i.e. `Dockerfile`, `1` and `2` directories).

```text
$ tree target/docker/stage
```

## Building Docker Image

```text
sbt docker:publishLocal
```

```text
$ sbt docker:publishLocal
...
Successfully tagged spark-docker-example:0.1.0
```

## minikube

**TIP:** Follow the steps in [Demo: Running Spark Application on minikube](https://books.japila.pl/apache-spark-internals/demo/running-spark-application-on-minikube/).

### Publishing Spark Image

Build and push JDK11-based Spark image to minikube's Docker daemon.

```text
$ ./bin/docker-image-tool.sh \
    -m \
    -t v3.0.1 \
    -b java_image_tag=11-jre-slim \
    build
...
Successfully tagged spark:v3.0.1
```

Point the shell to minikube's Docker daemon.

```text
eval $(minikube -p minikube docker-env)
```

```text
$ docker images
spark                                     v3.0.1          66983585811c   53 seconds ago   487MB
spark-docker-example                      0.1.0           45f2b54514bb   11 minutes ago   741MB
openjdk                                   11              27adf3d41a0d   47 hours ago     628MB
openjdk                                   8-jre-slim      cd08b38dfcae   2 days ago       187MB
...
```

### Publishing Spark Application Image

Point the shell to minikube's Docker daemon.

```text
eval $(minikube -p minikube docker-env)
```

Publish the image.

```text
sbt docker:publishLocal
```

List available images (that is supposed to include `spark-docker-example` and `openjdk` images).

```text
$ docker images
REPOSITORY                                TAG        IMAGE ID       CREATED         SIZE
spark-docker-example                      0.1.0      45f2b54514bb   4 seconds ago   741MB
openjdk                                   11         27adf3d41a0d   47 hours ago    628MB
...
```

```text
docker inspect spark-docker-example:0.1.0
```
