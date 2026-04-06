plugins {
    java
    application
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.protobuf") version "0.9.4"
}

group = "se.lnu"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")
    implementation("io.grpc:grpc-netty-shaded:1.68.0")
    implementation("io.grpc:grpc-protobuf:1.68.0")
    implementation("io.grpc:grpc-stub:1.68.0")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.5"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.68.0"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc")
            }
        }
    }
}

application {
    mainClass.set("se.lnu.Main")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
