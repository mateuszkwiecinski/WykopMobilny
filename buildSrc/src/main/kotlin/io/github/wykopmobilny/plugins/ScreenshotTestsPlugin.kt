package io.github.wykopmobilny.plugins

import com.android.build.gradle.BaseExtension
import com.karumi.shot.ShotExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class ScreenshotTestsPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("shot")

        (extensions.findByName("android") as BaseExtension).apply {
            defaultConfig {
                it.testInstrumentationRunner = "io.github.wykopmobilny.screenshots.ScreenshotTestRunner"
                it.testApplicationId = project.path.replaceFirstChar { "" }.replace(":", ".").replace("-", "_")
            }
            packagingOptions {
                it.resources.excludes += "META-INF/*"
            }
        }

        (extensions.findByName("shot") as ShotExtension).apply {
            applicationId = project.path.replaceFirstChar { "" }.replace(":", ".").replace("-", "_")
        }

        dependencies.apply {
            add("androidTestImplementation", project(":common:screenshot-test-helpers"))
        }

        Unit
    }
}
