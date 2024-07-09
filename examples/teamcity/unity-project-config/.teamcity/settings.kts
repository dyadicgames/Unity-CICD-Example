import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2024.03"

project {

    buildType(RunTests)
    buildType(Build)
}

object Build : BuildType({
    name = "Build"

    artifactRules = """
        %system.teamcity.projectName%/Builds/StandaloneWindows64 => %system.teamcity.projectName%.zip
        -:%system.teamcity.projectName%/Builds/StandaloneWindows64/%system.teamcity.projectName%_BackUpThisFolder_ButDontShipItWithYourGame/** => %system.teamcity.projectName%.zip
        -:%system.teamcity.projectName%/Builds/StandaloneWindows64/%system.teamcity.projectName%_BurstDebugInformation_DoNotShip/** => %system.teamcity.projectName%.zip
    """.trimIndent()

    vcs {
        root(DslContext.settingsRoot)

        checkoutMode = CheckoutMode.ON_SERVER
    }

    steps {
        step {
            id = "unity"
            type = "unity"
            param("executeMethod", "BuildScript.BuildPlayerHeadless")
            param("silentCrashes", "true")
            param("projectPath", "TGDF2024")
            param("noQuit", "true")
            param("noGraphics", "true")
            param("buildPlayer", "buildWindows64Player")
            param("buildTarget", "StandaloneWindows64")
        }
    }

    triggers {
        vcs {
        }
    }

    features {
        perfmon {
        }
    }
})

object RunTests : BuildType({
    name = "Run Tests"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        step {
            name = "Run Edit Mode Tests"
            id = "unity"
            type = "unity"
            param("silentCrashes", "true")
            param("testPlatform", "editmode")
            param("projectPath", "TGDF2024")
            param("noGraphics", "true")
            param("runEditorTests", "true")
        }
    }

    features {
        commitStatusPublisher {
            vcsRootExtId = "${DslContext.settingsRoot.id}"
            publisher = swarm {
                serverUrl = "http://helix-swarm:8085/"
                username = "reviewer"
                token = "credentialsJSON:0bb52d71-7f25-4b61-9497-a804338795cf"
                commentOnEvents = true
            }
        }
    }
})
