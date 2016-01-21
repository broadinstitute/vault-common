name := "Vault-Common"

val vaultOrg = "org.broadinstitute.dsde.vault"

organization := vaultOrg

// Canonical version
val versionRoot = "0.1"

// Get the revision, or -1 (later will be bumped to zero)
val versionRevision = ("git rev-list --count HEAD" #|| "echo -1").!!.trim.toInt

// Set the suffix to None...
val versionSuffix = {
  try {
    // ...except when there are no modifications...
    if ("git diff --quiet HEAD".! == 0) {
      // ...then set the suffix to the revision "dash" git hash
      Option(versionRevision + "-" + "git rev-parse --short HEAD".!!.trim)
    } else {
      None
    }
  } catch {
    case e: Exception =>
      None
  }
}

// Set the composite version
version := versionRoot + "-" + versionSuffix.getOrElse((versionRevision + 1) + "-SNAPSHOT")

val artifactory = "https://artifactory.broadinstitute.org/artifactory/"

resolvers += "artifactory-releases" at artifactory + "libs-release"

scalaVersion := "2.11.7"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

scalacOptions := Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8", "-target:jvm-1.8")

libraryDependencies ++= {
  val akkaV = "2.4.1"
  val sprayV = "1.3.3"
  Seq(
    "io.spray" %% "spray-routing" % sprayV
    , "io.spray" %% "spray-json" % "1.3.2"
    , "io.spray" %% "spray-client" % sprayV
    , "io.spray" %% "spray-testkit" % sprayV % "test"
    , "com.typesafe.akka" %% "akka-actor" % akkaV
    , "com.typesafe.akka" %% "akka-slf4j" % akkaV % "provided"
    , "org.scalatest" %% "scalatest" % "2.2.6" % "test"
    // -- Logging --
    , "org.slf4j" % "slf4j-api" % "1.7.13"
    , "ch.qos.logback" % "logback-classic" % "1.1.3" % "test"
  )
}

// Don't package the application.conf in the assembly
excludeFilter in(Compile, unmanagedResources) := HiddenFileFilter || "application.conf"

// Do include the application.conf for testing purposes
excludeFilter in(Test, unmanagedResources) := HiddenFileFilter

// SLF4J initializes itself upon the first logging call.  Because sbt
// runs tests in parallel it is likely that a second thread will
// invoke a second logging call before SLF4J has completed
// initialization from the first thread's logging call, leading to
// these messages:
//   SLF4J: The following loggers will not work because they were created
//   SLF4J: during the default configuration phase of the underlying logging system.
//   SLF4J: See also http://www.slf4j.org/codes.html#substituteLogger
//   SLF4J: com.imageworks.common.concurrent.SingleThreadInfiniteLoopRunner
//
// As a workaround, load SLF4J's root logger before starting the unit
// tests

// Source: https://github.com/typesafehub/scalalogging/issues/23#issuecomment-17359537
// References:
//   http://stackoverflow.com/a/12095245
//   http://jira.qos.ch/browse/SLF4J-167
//   http://jira.qos.ch/browse/SLF4J-97
testOptions in Test += Tests.Setup(classLoader =>
  classLoader
    .loadClass("org.slf4j.LoggerFactory")
    .getMethod("getLogger", classLoader.loadClass("java.lang.String"))
    .invoke(null, "ROOT")
)

autoAPIMappings := true

// autoAPIMappings isn't 100%. This is. http://stackoverflow.com/a/20919304/3320205
apiMappings ++= {
  val cp: Seq[Attributed[File]] = (fullClasspath in Compile).value
  def findManagedDependency(organization: String, name: String): File = {
    ( for {
      entry <- cp
      module <- entry.get(moduleID.key)
      if module.organization == organization
      if module.name.startsWith(name)
      jarFile = entry.data
    } yield jarFile
      ).head
  }
  Map(
    findManagedDependency("io.spray", "spray-routing") -> url("http://spray.io/documentation/1.1-SNAPSHOT/api/")
  )
}
