pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://papermc.io/repo/repository/maven-public/")
  }
}

rootProject.name = "PaperX"

include("PaperX-API", "PaperX-Server")