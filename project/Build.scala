import sbt._
import Keys._

object SknnBuild extends Build {

  object V {
    val depProject = "master"
  }

  object Projects {
    lazy val depProject = RootProject(uri("git://github.com/pelotom/effectful.git#%s".format(V.depProject)))
  }


  val simple = Project.defaultSettings ++ Seq(scalaVersion := "2.11.6", version := "1.0", fork in run := true, resolvers ++= res, libraryDependencies ++= libDeps)

  val libDeps = Seq(
    "org.scalaz" %% "scalaz-core" % "7.1.3",
    "org.scalaz" %% "scalaz-concurrent" % "7.1.3",
    "org.scalaz.stream" %% "scalaz-stream" % "0.7a",
    "org.spire-math" %% "spire" % "0.10.1",
    "org.scala-miniboxing.plugins" %% "miniboxing-runtime" % "0.4-SNAPSHOT"
  )

  val res = Seq(
    "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
    Resolver.sonatypeRepo("snapshots")
  )

  addCompilerPlugin("org.scala-miniboxing.plugins" %%
    "miniboxing-plugin" % "0.4-SNAPSHOT")

  // Library dependencies
  lazy val myProject = Project("sk-nn", file("."))
    .settings(Project.defaultSettings)
    .dependsOn(Projects.depProject)
    .settings(simple)

}
