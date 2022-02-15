import io.papermc.paperweight.util.constants.PAPERCLIP_CONFIG

plugins {
  java
  id("com.palantir.git-version") version "0.12.3"
  id("com.github.johnrengelman.shadow") version "7.1.1"
  id("io.papermc.paperweight.patcher") version "1.3.4"
}

val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
val git = versionDetails()

group = "dev.fiki.paperx"
version = "${providers.gradleProperty("mcVersion").get()}-${git.lastTag.substring(1)}.r${git.commitDistance}"

val isCiBuilding = System.getenv()["CI"] == "true"

repositories {
  mavenCentral()
  maven("https://papermc.io/repo/repository/maven-public/") {
    content { onlyForConfigurations(PAPERCLIP_CONFIG) }
  }
}

dependencies {
  remapper("net.fabricmc:tiny-remapper:0.8.1")
  decompiler("net.minecraftforge:forgeflower:1.5.498.22")
  paperclip("io.papermc:paperclip:3.0.2")
}

allprojects {
  apply(plugin = "java")
  apply(plugin = "maven-publish")

  java {
    toolchain {
      languageVersion.set(JavaLanguageVersion.of(17))
    }
  }
}

subprojects {
  tasks.withType<JavaCompile> {
    options.encoding = Charsets.UTF_8.name()
    options.release.set(17)
  }
  tasks.withType<Javadoc> {
    options.encoding = Charsets.UTF_8.name()
  }
  tasks.withType<ProcessResources> {
    filteringCharset = Charsets.UTF_8.name()
  }

  repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
  }
}

paperweight {
  serverProject.set(project(":PaperX-Server"))

  remapRepo.set("https://maven.fabricmc.net/")
  decompileRepo.set("https://files.minecraftforge.net/maven/")

  usePaperUpstream(providers.gradleProperty("paperRef")) {
    withPaperPatcher {
      apiPatchDir.set(layout.projectDirectory.dir("patches/api"))
      apiOutputDir.set(layout.projectDirectory.dir("PaperX-API"))

      serverPatchDir.set(layout.projectDirectory.dir("patches/server"))
      serverOutputDir.set(layout.projectDirectory.dir("PaperX-Server"))
    }
  }
}

val actionsTask by tasks.register("createGitHubActionsVars") {
  onlyIf { isCiBuilding }
  doFirst {
    println("::set-output name=paperclip::${tasks.createReobfPaperclipJar.get().outputZip.get().asFile.name}")
    println("::set-output name=bundler::${tasks.createReobfBundlerJar.get().outputZip.get().asFile.name}")
    println("::set-output name=version::${project.version}")
  }
}

tasks.createReobfPaperclipJar { finalizedBy(actionsTask) }