[![Apache Sling](https://sling.apache.org/res/logos/sling.png)](https://sling.apache.org)

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
