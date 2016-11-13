# Releasing

 1. Change the version in `gradle.properties` to a non-SNAPSHOT version.
 2. Update the `CHANGELOG.md` for the impending release.
 3. Update the `README.md` with the new version.
 4. `git commit -am "Prepare for release X.Y.Z."` (where X.Y.Z is the new version)
 5. `git tag -a X.Y.Z -m "Version X.Y.Z"` (where X.Y.Z is the new version)
 6. `./gradlew clean build`
 6. `./gradlew power-adapters:uploadArchives`
 6. `./gradlew power-adapters-data:uploadArchives`
 6. `./gradlew power-adapters-data-rx:uploadArchives`
 6. `./gradlew power-adapters-recyclerview-v7:uploadArchives`
 6. `./gradlew power-adapters-support-v4:uploadArchives`
 7. Update the `gradle.properties` to the next SNAPSHOT version.
 8. `git commit -am "Prepare next development version."`
 9. `git push && git push --tags`
 10. Visit [Sonatype Nexus](https://oss.sonatype.org/) and promote the artifact.

This project uses [semantic versioning](http://semver.org)