
allprojects {

    repositories {
        jcenter()
        mavenCentral()
    }
}

subprojects {
    version = "1.0"
}

val getProps by extra {
    fun(propName: String): String {
        val propsFile = rootProject.file("local.properties")
        return if (propsFile.exists()) {
            val props = java.util.Properties()
            props.load(java.io.FileInputStream(propsFile))
            props[propName] as String
        } else {
            ""
        }
    }
}
