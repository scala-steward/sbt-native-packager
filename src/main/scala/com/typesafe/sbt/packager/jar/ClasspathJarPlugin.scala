package com.typesafe.sbt.packager.archetypes.jar

import java.io.File
import java.util.jar.Attributes

import sbt.Package.ManifestAttributes
import sbt._
import sbt.Keys._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging

object ClasspathJarPlugin extends AutoPlugin {

  object autoImport {
    val packageJavaClasspathJar: TaskKey[File] = TaskKey[File](
      "packageJavaClasspathJar",
      "Creates a Java classpath jar that specifies the classpath in its manifest"
    )
  }
  import autoImport._

  override def requires = JavaAppPackaging

  override lazy val projectSettings: Seq[Setting[_]] = Defaults
    .packageTaskSettings(packageJavaClasspathJar, packageJavaClasspathJar / mappings) ++ Seq(
    packageJavaClasspathJar / mappings := Nil,
    packageJavaClasspathJar / artifactClassifier := Option("classpath"),
    packageJavaClasspathJar / packageOptions := {
      val classpath = (packageJavaClasspathJar / scriptClasspath).value
      val manifestClasspath = Attributes.Name.CLASS_PATH -> classpath.mkString(" ")
      Seq(ManifestAttributes(manifestClasspath))
    },
    packageJavaClasspathJar / artifactName := { (scalaVersion, moduleId, artifact) =>
      moduleId.organization + "." + artifact.name + "-" + moduleId.revision +
        artifact.classifier.fold("")("-" + _) + "." + artifact.extension
    },
    bashScriptDefines / scriptClasspath := Seq((packageJavaClasspathJar / artifactPath).value.getName),
    batScriptReplacements / scriptClasspath := Seq((packageJavaClasspathJar / artifactPath).value.getName),
    Universal / mappings += {
      val classpathJar = packageJavaClasspathJar.value
      classpathJar -> ("lib/" + classpathJar.getName)
    }
  )
}
