# Publishing to Maven Central

## Prerequisites

- You PGP key needs to be uploaded to one of the common PGP key servers, e.g.
  `https://pgp.mit.edu/`
- You need to be registered with Sonatype and allowed to publish our project
- Generate a user token at `https://central.sonatype.com/account`
- It will show you the `<server>` entry for your `~/.m2/settings.xml` file. Paste the entry there.


## Publish

Ensure that `JAVA_HOME` is set properly:

```bash
export JAVA_HOME=/usr/lib/jvm/default
```

Specify a PGP key and publish:

```bash
mvn clean deploy -P publish-central -Dgpg.keyname="KEY_ID"
```
