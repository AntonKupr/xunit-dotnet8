import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.
*/

version = "2019.2"

project {
    description = "xUnit .NET 8 Project"

    // Define VCS Root
    val vcsRoot = GitVcsRoot {
        id("XUnitDotNet8_VcsRoot")
        name = "xunit-dotnet8 git repository"
        url = "https://github.com/yourusername/xunit-dotnet8.git" // Replace with actual repository URL
        branch = "refs/heads/main"
        branchSpec = "+:refs/heads/*"
    }
    vcsRoot(vcsRoot)

    // Build Configuration
    buildType {
        id("XUnitDotNet8_Build")
        name = "Build and Test"

        vcs {
            root(vcsRoot)
        }

        steps {
            // Restore NuGet packages
            script {
                name = "Restore NuGet packages"
                scriptContent = "dotnet restore"
            }

            // Build the solution
            script {
                name = "Build solution"
                scriptContent = "dotnet build --configuration Release"
            }

            // Run tests
            script {
                name = "Run tests"
                scriptContent = "dotnet test --configuration Release --no-build"
            }

            // Package the application (if needed)
            script {
                name = "Package application"
                scriptContent = "dotnet publish --configuration Release --output ./publish"
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
    }

    // Deployment Configuration
    buildType {
        id("XUnitDotNet8_Deploy")
        name = "Deploy"
        
        dependencies {
            snapshot(RelativeId("XUnitDotNet8_Build")) {
                onDependencyFailure = FailureAction.FAIL_TO_START
            }
        }

        steps {
            // Example deployment step - adjust as needed for your environment
            script {
                name = "Deploy application"
                scriptContent = """
                    echo "Deploying application..."
                    # Add your deployment commands here
                    # For example: copying files to a server, running deployment scripts, etc.
                """
            }
        }

        // Optional: Add deployment triggers if needed
        // For example, manual trigger or scheduled deployment
    }
}