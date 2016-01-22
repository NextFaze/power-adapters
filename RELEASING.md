# Releasing

1. `git flow release start X.Y.Z`
2. Change the version in `gradle.properties` to a non-SNAPSHOT version.
3. Update the `CHANGELOG.md` for the impending release.
4. Update the `README.md` with the new version.
5. `git commit -am "Version X.Y.Z"` (where X.Y.Z is the new version)
6. `./gradlew clean uploadArchives`
7. `git flow release finish X.Y.Z`
8. Update the `gradle.properties` to the next SNAPSHOT version.
9. `git commit -am "Version X.Y.Z-SNAPSHOT"`
10. `git push && git push --tags`
11. Visit [Sonatype Nexus](https://oss.sonatype.org/) and promote the artifact.

This project uses [semantic versioning](http://semver.org)