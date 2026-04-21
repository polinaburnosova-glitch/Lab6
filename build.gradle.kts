plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Jackson для работы с XML
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.15.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")

    // Apache Commons Lang3 для ToStringBuilder
    implementation("org.apache.commons:commons-lang3:3.13.0")
}

application {
    mainClass.set("server.ServerMain")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<JavaExec> {
    systemProperty("file.encoding", "UTF-8")
}


tasks.withType<JavaExec> {
    systemProperty("file.encoding", "UTF-8")
    systemProperty("console.encoding", "UTF-8")
    jvmArgs = listOf("-Dfile.encoding=UTF-8")
}

dependencies {
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.15.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")
    implementation("org.apache.commons:commons-lang3:3.13.0")
}

tasks.register("runClient", JavaExec::class) {
    mainClass.set("client.ClientMain")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "server.ServerMain"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}