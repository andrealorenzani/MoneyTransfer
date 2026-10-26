name := "MoneyTransfer"
version := "2.0.0"
organization := "name.lorenzani.andrea"
scalaVersion := "3.10.0"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Wunused:all")

libraryDependencies ++= Seq(
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % "2.42.0",
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.42.0" % "provided",
  "org.scalameta"                         %% "munit"                 % "1.3.6"  % Test
)

Test / fork := true
