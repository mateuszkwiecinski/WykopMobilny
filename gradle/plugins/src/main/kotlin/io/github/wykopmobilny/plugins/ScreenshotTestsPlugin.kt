package io.github.wykopmobilny.plugins

import com.android.build.gradle.BaseExtension
import com.facebook.testing.screenshot.build.ScreenshotsPluginExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class ScreenshotTestsPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("io.github.usefulness.screenshot-testing-plugin")

        (extensions.findByName("android") as BaseExtension).apply {
            defaultConfig {
                it.testInstrumentationRunner = "io.github.wykopmobilny.screenshots.ScreenshotTestRunner"
                it.testApplicationId = project.path.replaceFirstChar { "" }.replace(":", ".").replace("-", "_")
            }
            packagingOptions {
                it.resources.excludes += "META-INF/*"

                testOptions {
                    it.animationsDisabled = true
                }
            }

            (extensions.getByType(ScreenshotsPluginExtension::class.java)).apply {
                multipleDevices = false
                pythonExecutable = "python3"
                failureDir = "$buildDir/failedScreenshots"
            }
        }

        dependencies.apply {
            add("androidTestImplementation", project(":common:screenshot-test-helpers"))
        }

        Unit
    }
}
