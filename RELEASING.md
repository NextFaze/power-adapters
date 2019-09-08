# Releasing

 1. Change the version in `gradle.properties` to a non-SNAPSHOT version.
 1. Update the `CHANGELOG.md` for the impending release.
 1. Update the `README.md` with the new version.
 1. `git commit -am "Prepare for release X.Y.Z."` (where X.Y.Z is the new version)
 1. `git tag -a X.Y.Z -m "Version X.Y.Z"` (where X.Y.Z is the new version)
 1. `./gradlew clean build`
 1. `./gradlew uploadArchives --no-parallel`
 1. Update the `gradle.properties` to the next SNAPSHOT version.
 1. `git commit -am "Prepare next development version."`
 1. `git push && git push --tags`
 1. Visit [Sonatype Nexus](https://oss.sonatype.org/) and promote the artifact.

This project uses [semantic versioning](http://semver.org)
