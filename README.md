# CoinGecko-Java
![GitHub](https://img.shields.io/github/license/osslabz/coingecko-java)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/osslabz/coingecko-java/build-on-push.yml?branch=dev&label=build&logo=git)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/osslabz/coingecko-java/release.yml?branch=dev&label=perform-release&logo=semanticrelease)
[![Reproducible Builds](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/jvm-repo-rebuild/reproducible-central/master/content/net/osslabz/coingecko-java/badge.json)](https://github.com/jvm-repo-rebuild/reproducible-central/blob/master/content/net/osslabz/coingecko-java/README.md)
[![Maven Central](https://img.shields.io/maven-central/v/net.osslabz/coingecko-java?label=Maven%20Central)](https://search.maven.org/artifact/net.osslabz/coingecko-java)

Java wrapper for the CoinGecko API.
<p align="center">
    <img src="https://i.ibb.co/sRLCZk2/java-gecko-200.png" alt="CoinGecko-Java Logo"/>
</p>


This is a (synced) fork of [Philipinho/CoinGecko-Java](https://github.com/Philipinho/CoinGecko-Java) that is properly released on [Maven Central](https://search.maven.org/artifact/net.osslabz/coingecko-java).

Once [Enable CI and Automated Release Management #41](https://github.com/Philipinho/CoinGecko-Java/pull/41) is merged and the original project is published this fork will be discontinued.

Usage
---------

### Maven

```xml
<dependency>
    <groupId>net.osslabz</groupId>
    <artifactId>coingecko-java</artifactId>
    <version>1.2.0</version>
</dependency>
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'net.osslabz:coingecko-java:1.2.0'
}
```

### Gradle (Kotlin)

```kotlin
dependencies {
    implementation ("net.osslabz:coingecko-java:1.2.0")
}
```

### Snapshots

Every push to `dev` publishes the next version as a `-SNAPSHOT` to Central's snapshot repository. Maven doesn't
search that repository by default, so a build that wants a snapshot declares it:

```xml
<repositories>
    <repository>
        <id>central-snapshots</id>
        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

**For further info see below the original README.**

---

## Usage
This API client covers all CoinGecko's API endpoints and i'll try to update it when new endpoints are added.

For complete API documentation please refer to https://www.coingecko.com/api/docs/v3.

For examples Goto: <a href="https://github.com/Philipinho/CoinGecko-Java/tree/master/src/test/java/com/litesoftwares/coingecko/examples">Examples</a>.

```
CoinGeckoApiClient client = new CoinGeckoApiClientImpl();
client.ping();
client.shutdown();
```

To get price of a currency in USD
```
client.getPrice("bitcoin",Currency.USD);
```

## License
MIT License

Copyright (c) 2019 Philip Okugbe

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.