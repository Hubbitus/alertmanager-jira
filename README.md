# tracking-receiver

Purpose and description see in the [documentation](https://confluence.gid.team/pages/viewpage.action?pageId=44503292) and task [DATA-1188](https://jira-new.gid.team/browse/DATA-1188).

## Tech overview
This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell
./gradlew build
```
It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell
./gradlew build -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell
./gradlew build -Dquarkus.package.type=native
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/tracking-receiver-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/gradle-tooling.

## Creating a native executable and pack into container

```shell
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true
```
or:
```shell
./gradlew imageBuild -Dquarkus.package.type=native
```

See [documentation](https://quarkus.io/guides/building-native-image#creating-a-container) for available customizations.

## Related Guides

- SmallRye Reactive Messaging - Kafka Connector ([guide](https://quarkus.io/guides/kafka-reactive-getting-started)): Connect to Kafka with Reactive Messaging

## MyTracker sample statistic send page included

Please look at [_DEV.scripts/counter.htm](_DEV.scripts/counter.htm) file.

## Running in a container

```shell
podman run -it --rm --name traching-receiver-manual \
    -p 8080:8080 \
    -v /home/pasha/@Projects/@DATA/kafka.scripts/conf/DEV/:/work/_conf:Z,ro \
        localhost/local/tracking-receiver:latest pwd
```