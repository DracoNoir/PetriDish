import com.soywiz.korge.gradle.KorgeGradlePlugin
import com.soywiz.korge.gradle.korge

buildscript {
    repositories {
        mavenLocal()
        maven { url = uri("https://dl.bintray.com/korlibs/korlibs") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.soywiz.korlibs.korge.plugins:korge-gradle-plugin:2.0.8.1")
    }
}

apply<KorgeGradlePlugin>()

korge {
    id = "com.nocteq.petridish"
    name = "PetriDish"

    // To enable all targets at once
//    targetAll()

    // To selectively enable targets
    targetJvm()
    targetJs()
    targetDesktop()
    targetIos()
    targetAndroidIndirect() // targetAndroidDirect()
}
