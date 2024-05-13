
# How to make a release

## From your local machine

### Configuration

First, create a `gradle.properties` file in the root of the project with the following properties:

```properties
signing.key=exported_ascii_armored_key
signing.password = gpg_key_password
mavenCentralUsername=username
mavenCentralPassword=the_password
```

More information on the release process can be found here [here](https://vanniktech.github.io/gradle-maven-publish-plugin/central/).

You will need to have gpg setup on your machine. To obtain the in memory signing key run the following command:

```shell
gpg --export-secret-keys --armor <key id> | grep -v '\-\-' | grep -v '^=.' | tr -d '\n'
```

### Publishing a snapshot

To release a snapshot version, run the following in the root directory of the repository:

```shell
$> ./gradlew publishAllPublicationsToMavenCentralRepository
```

If the `version` specified in the `build.gradle.kts` file is `1.2.3` then this script will release a 
SNAPSHOT version that looks something like this: `1.2.3.20210419134847-SNAPSHOT` where the `20210419134847`
portion is the year, month, day, hour, minutes, seconds that the build was cut.

### Publishing a release

To release a non-snapshot version, run the following in the root directory of the repository:

```shell
$> ./gradlew publishAndReleaseToMavenCentral --no-configuration-cache
```

Be sure that the `version` in the `build.gradle.kts` file is what you want it to be.

## Continuous integration & deployment (CI/CD)

In the case of a CI/CD machine you may not want to have the keyring file(s) on your machine, in this
case you can instead use an ascii armored version of the gpg key by passing the following arguments:

```shell
$> ./gradlew \
  -Psigning.key=${ASCII_ARMORED_VERSION_OF_GPG_KEY} \
  -Psigning.password=${GPG_KEY_PASSWORD} \
  -PmavenCentralUsername=... \
  -PmavenCentralPassword=... \
  publishAndReleaseToMavenCentral --no-configuration-cache
```

## GitHub Actions

There are two GitHub Actions configured:

- SNAPSHOT: On every commit to the `main` branch a build is performed and if successful it is deployed as a snapshot version.
- RELEASE: Whenever a tag is created with the pattern of `vXXX` a version with the name XXX is built and if successful deployed as a release version.

The following GitHub repository secrets configure these actions:

- `FLOW_JVM_SDK_CICD_PUBLISH_ENABLED`: (optional) Must be `true` for the publishing of artifacts to happen (defaults to `false`)
- `FLOW_JVM_SDK_SIGNING_KEY`: (required if publish enabled) ascii armored version of the pgp key for signing releases
- `FLOW_JVM_SDK_SIGNING_PASSWORD`: (required if publish enabled) password to the pgp key
- `FLOW_JVM_SDK_SONATYPE_USERNAME`: (required if publish enabled) sonatype username
- `FLOW_JVM_SDK_SONATYPE_PASSWORD`: (required if publish enabled) sonatype password

The Github Actions take care of starting/stopping an emulator for the unit tests.
