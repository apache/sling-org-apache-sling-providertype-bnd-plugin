[![Apache Sling](https://sling.apache.org/res/logos/sling.png)](https://sling.apache.org)

[![Build Status](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-providertype-bnd-plugin/job/master/badge/icon)](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-providertype-bnd-plugin/job/master/)
[![Test Status](https://img.shields.io/jenkins/tests.svg?jobUrl=https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-providertype-bnd-plugin/job/master/)](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-providertype-bnd-plugin/job/master/test/?width=800&height=600)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=apache_sling-org-apache-sling-providertype-bnd-plugin&metric=coverage)](https://sonarcloud.io/dashboard?id=apache_sling-org-apache-sling-rewriter)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=apache_sling-org-apache-sling-rewriter&metric=alert_status)](https://sonarcloud.io/dashboard?id=apache_sling-org-apache-sling-providertype-bnd-plugin)
[![JavaDoc](https://www.javadoc.io/badge/org.apache.sling//sling-org-apache-sling-providertype-bnd-plugin.svg)](https://www.javadoc.io/doc/org.apache.sling//sling-org-apache-sling-providertype-bnd-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/sling-org-apache-sling-providertype-bnd-plugin/badge.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.apache.sling%22%20a%3A%22sling-org-apache-sling-providertype-bnd-plugin%22)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

# Apache Sling Provider Type Checker Bnd Plugin

This module contains a [Bnd plugin][bnd-plugins] enforcing that no class of the current bundle extends or implements a [provider type][provider-type]. Note that *using* a provider type (i.e. calling its methods without implementing or extending it) is still allowed (even for consumers).

That ensures that the `import-package` version ranges are not narrow but [broad][semantic-versioning] and the risk that the bundle is incompatible with newer versions of its dependent bundles is less likely.

# Usage

For usage with Maven the Bnd plugin has to be added to the plugin dependencies of `bnd-maven-plugin` (or `maven-bundle-plugin`) like this:

```
<plugin>
    <groupId>biz.aQute.bnd</groupId>
    <artifactId>bnd-maven-plugin</artifactId>
    <extensions>true</extensions>
    <dependencies>
        <dependency>
            <groupId>org.apache.sling</groupId>
            <artifactId>org.apache.sling.providertype.bnd-plugin</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
</plugin>
```

In addition the `bnd.bnd` file needs to register the Bnd plugin with the [plugin instruction](https://bnd.bndtools.org/instructions/plugin.html)

```
-plugin.providertype:org.apache.sling.providertype.bndplugin.ProviderTypeScanner
```

## Configuration

To explicitly ignore certain provider types (i.e. don't fail when these are extended/implemented) one can use the attribute `ignored` with one or multiple comma-separated fully qualified provider type names. For example

```
-plugin.providertype:org.apache.sling.providertype.bndplugin.ProviderTypeScanner;ignored=org.apache.jackrabbit.api.security.user.User
```

## Prerequisites

* Bnd 6.0 or newer (integrated in `bnd-maven-plugin` version 6.0.0+ or `maven-bundle-plugin` version 5.1.5+)
* Java 11 or newer

# Provider Type Information

The information whether a type (i.e. a class or interface) is designed to be extended/implemented only by providers or also by consumers is determined originally from the the annotations [`@org.osgi.annotation.versioning.ProviderType`][provider-type] or [`@org.osgi.annotation.versioning.ConsumerType`][consumer-type].
In order to speed up the check [the annotation is evaluated and extracted into a dedicated JSON file named `META-INF/api-info.json` when generating the apis jar](https://issues.apache.org/jira/browse/SLING-12135) and being looked up from there within this plugin.


[bnd-plugins]: https://bnd.bndtools.org/chapters/870-plugins.html
[provider-type]: https://docs.osgi.org/javadoc/osgi.annotation/8.0.0/org/osgi/annotation/versioning/ProviderType.html
[consumer-type]: https://docs.osgi.org/javadoc/osgi.annotation/8.0.0/org/osgi/annotation/versioning/ConsumerType.html
[semantic-versioning]: https://docs.osgi.org/whitepaper/semantic-versioning/060-importer-policy.html
