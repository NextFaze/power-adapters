# Releasing

 1. Change the version in `gradle.properties` to a non-SNAPSHOT version.
 1. Update the `CHANGELOG.md` for the impending release.
 1. Update the `README.md` with the new version.
 1. `git commit -am "Prepare for release X.Y.Z."` (where X.Y.Z is the new version)
 1. `git tag -a X.Y.Z -m "Version X.Y.Z"` (where X.Y.Z is the new version)
 1. `./gradlew clean build`
 1. `./gradlew power-adapters:uploadArchives`
 1. `./gradlew power-adapters-data:uploadArchives`
 1. `./gradlew power-adapters-data-kotlin:uploadArchives`
 1. `./gradlew power-adapters-data-rx:uploadArchives`
 1. `./gradlew power-adapters-data-rx-kotlin:uploadArchives`
 1. `./gradlew power-adapters-kotlin:uploadArchives`
 1. `./gradlew power-adapters-recyclerview-v7:uploadArchives`
 1. `./gradlew power-adapters-recyclerview-v7-kotlin:uploadArchives`
 1. `./gradlew power-adapters-rx:uploadArchives`
 1. `./gradlew power-adapters-rx-kotlin:uploadArchives`
 1. `./gradlew power-adapters-sample:uploadArchives`
 1. `./gradlew power-adapters-support-v4:uploadArchives`
 1. Update the `gradle.properties` to the next SNAPSHOT version.
 1. `git commit -am "Prepare next development version."`
 1. `git push && git push --tags`
 1. Visit [Sonatype Nexus](https://oss.sonatype.org/) and promote the artifact.

This project uses [semantic versioning](http://semver.org)