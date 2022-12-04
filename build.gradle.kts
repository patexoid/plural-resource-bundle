plugins {
    `java-library`
    `maven-publish`
    id("com.palantir.git-version") version "0.15.0"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    api("org.slf4j:jcl-over-slf4j:1.7.25")
    testImplementation("junit:junit:4.12")
}
val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
val details = versionDetails()
group = "com.patex"
version =
    if (details.commitDistance == 0) details.lastTag else (details.lastTag + "-" + details.commitDistance + "-" + details.gitHash)
description = "plural-resource-bundle"
java.sourceCompatibility = JavaVersion.VERSION_1_8

println(version)
java {
    withSourcesJar()
    withJavadocJar()
}

if(details.commitDistance==0) {
    publishing {
        repositories {
            maven {
                name = "github"
                url = uri("https://maven.pkg.github.com/patexoid/repo")
                credentials {
                    username = System.getenv("USERNAME")
                    password = System.getenv("TOKEN")
                }
            }
        }
        publications.create<MavenPublication>("github") {
            from(components["java"])
        }
    }
}
tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}
