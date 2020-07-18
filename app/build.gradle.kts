import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.Service

val getProps = rootProject.extra["getProps"] as (String) -> String

plugins {
    kotlin("jvm") version "1.3.72"
    id("org.hidetake.ssh") version "2.10.1"
}

val mainPackage = "com.zelgius.driver.eink.app"
group = mainPackage
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.pi4j:pi4j-core:1.2")
    implementation("com.github.mhashim6:Pi4K:0.1")
    implementation(project(":driver"))
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.4")
    testImplementation ("junit:junit:4.+")

}


tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

lateinit var jar: Jar
val jarTask = tasks.register("fatJar", Jar::class.java) {
    jar = this
    jar.apply {
        logger.warn("Building jar")
        manifest {
            attributes(
                "Implementation-Title" to "Gradle Jar File Example",
                "Implementation-Version" to archiveVersion.get(),
                "Main-Class" to "$mainPackage.MainKt"
            )
        }
        archiveBaseName.set(project.name + "-all")
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        with(tasks.jar.get() as CopySpec)
    }
}

var raspberry = remotes.create("raspberry") {
    host = "192.168.1.31"
    user = "pi"
    password = getProps("password")
}


val deploy = tasks.create("deploy") {
    doLast {
        ssh.runSessions {
            session(raspberry) {
                try {
                    execute("sudo rm ${jar.archiveFile.get().asFile.name}")
                } catch (e: Exception) {
                    logger.error(e.message)
                }
                put(jar.archiveFile.get().asFile, File("/home/pi/"))
                logger.warn(execute("sudo java -jar ${jar.archiveFile.get().asFile.name}"))
            }
        }
    }
}

val deployDebug = tasks.create("deployDebug") {
    doLast {
        ssh.runSessions {
            session(raspberry) {
                try {
                    execute("sudo rm ${jar.archiveFile.get().asFile.name}")
                } catch (e: Exception) {
                    logger.error(e.message)
                }
                put(jar.archiveFile.get().asFile, File("/home/pi/"))
                logger.warn(execute("sudo java -jar ${jar.archiveFile.get().asFile.name} -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=5005 " +
                        " -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false " +
                        " -Djava.rmi.server.hostname=raspberrypi"))
            }
        }
    }
}

deploy.dependsOn(jarTask)
deployDebug.dependsOn(jarTask)

fun Service.runSessions(action: RunHandler.() -> Unit) =
    run(delegateClosureOf(action))

fun RunHandler.session(vararg remotes: Remote, action: SessionHandler.() -> Unit) =
    session(*remotes, delegateClosureOf(action))

fun SessionHandler.put(from: Any, into: Any) =
    put(hashMapOf("from" to from, "into" to into))

