plugins {
    java
}

group = "com.lab7"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
    implementation("org.postgresql:postgresql:42.7.3")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    (options as? StandardJavadocDocletOptions)?.let {
        it.charSet = "UTF-8"
        it.docEncoding = "UTF-8"
        it.addStringOption("Xdoclint:none", "-quiet")
    }
    isFailOnError = false
}

tasks.jar {
    manifest {
        attributes["Main-Class"] to "server.ServerMain"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}