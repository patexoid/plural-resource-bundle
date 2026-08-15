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
    testImplementation("junit:junit:4.13.2")
}
val versionDetails = extra["versionDetails"] as groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails>
val details = versionDetails()
group = "com.patex"
version =
    if (details.commitDistance == 0) details.lastTag else (details.lastTag + "-" + details.commitDistance + "-" + details.gitHash)
description = "plural-resource-bundle"

println(version)
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
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
    // Emit bytecode/API restricted to Java 17 (two LTS releases back from
    // the build toolchain, 25: 25 -> 21 -> 17) so the published jar stays
    // usable on older LTS runtimes, not just the one it was built with.
    options.release.set(17)
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}
