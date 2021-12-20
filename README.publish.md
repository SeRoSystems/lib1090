# Publishing to Central

## Prerequisites

- You PGP key needs to be uploaded to one of the common PGP key servers, e.g.
  `https://pgp.mit.edu/`
- You need to be registered with Sonatype and allowed to publish our project
- Enter your Sonatype credentials in `~/.m2/settings.xml:`

```xml
<settings>
    <servers>
        <server>
            <id>ossrh-sero</id>
            <username>SONATYPE_JIRA_USERNAME</username>
            <password>SECRET</password>
        </server>
    </servers>
</settings>
```


## Publish

Ensure that `JAVA_HOME` is set properly:

```bash
export JAVA_HOME=/usr/lib/jvm/default
```

Specify a PGP key and publish:

```bash
mvn clean deploy -P publish-central -Dgpg.keyname="KEY_ID"
```
