plugins {
    java
    id ("org.jetbrains.kotlin.jvm") version "1.3.72"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    // junit 5
    testImplementation ("org.junit.jupiter:junit-jupiter-api:5.7.0-M1")
    testImplementation ("org.junit.jupiter:junit-jupiter-params:5.7.0-M1")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.7.0-M1")
    api ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.4")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    test {
        useJUnitPlatform()
    }
}
