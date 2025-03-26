import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetBuild
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetTest
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetRestore

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in IntelliJ IDEA, open the 'TeamCity'
tool window (View -> Tool Windows -> TeamCity), then click the
'Debug' button in the toolbar and select the desired settings file.
*/

version = "2019.2"

project {
    description = "XUnitCore - .NET 8.0 xUnit Testing Project"

    // Define Main VCS Root
    val mainVcsRoot = DslContext.settingsRoot

    // Build Configuration for building and testing the solution
    buildType {
        id("Build_And_Test")
        name = "Build and Test"
        description = "Builds the solution and runs xUnit tests"

        vcs {
            root(mainVcsRoot)
        }

        steps {
            // Restore NuGet packages
            dotnetRestore {
                name = "Restore packages"
                projects = "XUnitCore.sln"
            }

            // Build the solution
            dotnetBuild {
                name = "Build solution"
                projects = "XUnitCore.sln"
                configuration = "Release"
            }

            // Run tests
            dotnetTest {
                name = "Run xUnit tests"
                projects = "PrimeService.Tests/PrimeService.Tests.csproj"
                configuration = "Release"
                framework = "net8.0"
                param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
            }
        }

        triggers {
            vcs {
                branchFilter = "+:*"
            }
        }

        features {
            perfmon {
            }
        }

        requirements {
            exists(".NET Core SDK 8.0")
        }
    }

    // Build Configuration for deployment
    buildType {
        id("Deploy")
        name = "Deploy"
        description = "Deploys the application"

        vcs {
            root(mainVcsRoot)
        }

        steps {
            // Build the solution in Release mode
            dotnetBuild {
                name = "Build solution for deployment"
                projects = "XUnitCore.sln"
                configuration = "Release"
            }

            // Publish the application
            script {
                name = "Publish application"
                scriptContent = """
                    dotnet publish PrimeService/PrimeService.csproj -c Release -o ./publish
                """.trimIndent()
            }
        }

        dependencies {
            snapshot(RelativeId("Build_And_Test")) {
                onDependencyFailure = FailureAction.FAIL_TO_START
            }
        }

        requirements {
            exists(".NET Core SDK 8.0")
        }
    }
}
