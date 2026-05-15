pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AssistXMonitor"
include(":app")

// Suggested edit: run terminal command to check Java
// {
//   "tool": "run_terminal_command",
//   "arguments": {
//     "command": "ls /home/assistx/jdk17/bin/java && /home/assistx/jdk17/bin/java -version"
//   }
// }
