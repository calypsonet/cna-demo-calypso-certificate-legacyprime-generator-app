plugins {
  java
  application
  id("com.diffplug.spotless") version "8.2.1"
}

group = "org.calypsonet.certificate.demo"

version = "1.0.0"

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  // Calypso Certificate APIs
  implementation(
      "org.calypsonet:calypsonet-terminal-calypso-certificate-legacyprime-java-api:0.1.0-SNAPSHOT"
  )
  implementation(
      "org.calypsonet:calypsonet-terminal-calypso-certificate-legacyprime-java-lib:0.1.0-SNAPSHOT"
  )

  // Utilities
  implementation("org.eclipse.keyple:keyple-util-java-lib:2.4.0")

  // Bouncy Castle for crypto operations
  implementation("org.bouncycastle:bcprov-jdk18on:1.79")
  implementation("org.bouncycastle:bcpkix-jdk18on:1.79")

  // Logging
  implementation("org.slf4j:slf4j-api:1.7.36")
  implementation("org.slf4j:slf4j-simple:1.7.36")
}

application { mainClass.set("org.calypsonet.certificate.demo.CalypsoCertificateDemo") }

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

tasks {
  spotless {
    java {
      target("src/**/*.java")
      licenseHeaderFile("${project.rootDir}/LICENSE_HEADER")
      importOrder("java", "javax", "org", "com", "")
      removeUnusedImports()
      googleJavaFormat()
    }
    kotlinGradle {
      target("**/*.kts")
      ktfmt()
    }
  }

  register<JavaExec>("runWithFiles") {
    group = "application"
    description = "Run CalypsoCertificateDemoWithFiles (advanced demo with PEM files)"
    mainClass.set("org.calypsonet.certificate.demo.CalypsoCertificateDemoWithFiles")
    classpath = sourceSets.main.get().runtimeClasspath
  }

  withType<JavaCompile> { options.encoding = "UTF-8" }
}
