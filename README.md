# vault-common
Vault Common Library

## IntelliJ Setup
* Configure the SBT plugin
* Open this source directory with File -> Open
* In the Import Project dialog, check "Create directories for empty content roots automatically" and set your Project SDK to 1.8 (TODO: do we actually require 1.8?)

## Bash Setup
Ensure the following prerequisites are installed. On a Mac, `sbt` and `liquibase` may be easily installed via [Homebrew](http://brew.sh).
* [sbt](http://scala-sbt.org)
* [openam](https://forgerock.org/openam)

## Plugins
* [spray-routing](http://spray.io/documentation/1.2.2/spray-routing/)
* [spray-json](https://github.com/spray/spray-json)
* [spray-client](http://spray.io/documentation/1.2.2/spray-routing/)
* [spray-testkit](http://spray.io/documentation/1.2.2/spray-routing/)
* [akka actors](http://akka.io/)
* [slf4j](http://www.slf4j.org/)
* [scalatest](http://scalatest.org)
* [logback](http://logback.qos.ch/)

## Development Notes

### Including the library via sbt

Add the following library dependency to include `vault-common`:

```scala
libraryDependencies +=
  "org.broadinstitute.dsde.vault.datamanagement" %% "vault-common" % "0.1-SNAPSHOT"
```

### Configuration of OpenAM

The repository `application.conf` does _not_ contain the deployment URI for OpenAM. Before running an application using vault-common, configure the URI inside your local copy of `application.conf`.

However if you will be actively developing across the codebase, and don't want to edit the `application.conf`, you may also set the deployment URI via the command line or via the `.sbtopts` file.

Use the `-Dkey=value` syntax for setting system properties on the command line.

```bash
sbt \
    -Dopenam.deploymentUri=...
```

Alternatively, you may set system properties in `.sbtopts`. The additional options in the file will load every time you run `sbt`.

```bash
$ cat .sbtopts | grep openam.deploymentUri
-Dopenam.deploymentUri=...
$
```

The non-production test code is able to login to OpenAM and generate a token. Parts of the configuration for authenticating to the server are setup in the test `application.conf` file, except for the username and password. You must specify these two properties via `openam.username` and `openam.password`.

```bash
$ cat .sbtopts | grep openam
-Dopenam.deploymentUri=...
-Dopenam.username=...
-Dopenam.password=...
$
```

### Testing

Run a basic test using the normal `sbt test`.

```bash
sbt \
    test
```
