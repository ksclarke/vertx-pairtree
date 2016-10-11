# vertx-pairtree [![Build Status](http://img.shields.io/travis/ksclarke/vertx-pairtree/master.svg?style=flat)](https://travis-ci.org/ksclarke/vertx-pairtree) [![Codacy coverage](https://img.shields.io/codacy/coverage/c44df2d9c89a4809896914fd1a40bedd.svg?maxAge=2592000)](https://api.codacy.com/project/badge/Coverage/ebf45038ace1469e842989f8d860ed1c)](https://www.codacy.com/app/ksclarke/vertx-pairtree?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ksclarke/vertx-pairtree&amp;utm_campaign=Badge_Coverage) [![Codacy grade](https://img.shields.io/codacy/grade/e27821fb6289410b8f58338c7e0bc686.svg?maxAge=2592000)](https://api.codacy.com/project/badge/Grade/ebf45038ace1469e842989f8d860ed1c)](https://www.codacy.com/app/ksclarke/vertx-pairtree?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ksclarke/vertx-pairtree&amp;utm_campaign=Badge_Grade) [![Maven Central](https://img.shields.io/maven-central/v/info.freelibrary/vertx-pairtree.svg)](http://mvnrepository.com/artifact/info.freelibrary/vertx-pairtree) [![MIT license](http://img.shields.io/badge/license-MIT-brightgreen.svg)](http://opensource.org/licenses/MIT)

A file system or S3 backed [Pairtree](https://wiki.ucop.edu/display/Curation/PairTree) library for use with the [Vert.x](http://vertx.io/) tool-kit. To use it, include its Jar file in your Vert.x project's classpath.

### Getting Started

To check out and build the project, type on the command line:

    git clone https://github.com/ksclarke/vertx-pairtree.git
    cd vertx-pairtree
    mvn install

This will run the build and the unit tests for the file system back end. Right now, the only tests for the S3 back end are integration tests. To run a build with them, you will need to put the following properties in your [settings.xml file](https://maven.apache.org/settings.html) and run the build with the `s3_it` profile:

    <vertx.pairtree.bucket>vertx-pairtree-tests</vertx.pairtree.bucket>
    <vertx.pairtree.access_key>YOUR_ACCESS_KEY</vertx.pairtree.access_key>
    <vertx.pairtree.secret_key>YOUR_SECRET_KEY</vertx.pairtree.secret_key>

Or, you can supply the required properties on the command line (with the same `s3_it` profile) when you build the project:

    mvn install -Ps3_it -Dvertx.pairtree.bucket=vertx-pairtree-tests -Dvertx.pairtree.access_key=YOUR_ACCESS_KEY -Dvertx.pairtree.secret_key=YOUR_SECRET_KEY

The YOUR_ACCESS_KEY and YOUR_SECRET_KEY values obviously need to be replaced with real values. Within AWS' Identity and Access Management service you can configure a user that has permission to perform actions on the vertx-pairtree-tests S3 bucket (which you'll also have to create). For an example of the IAM inline user policy for an S3 bucket, consult the [example JSON file](https://github.com/ksclarke/freelib-utils/blob/master/src/test/resources/sample-iam-policy.json) in the project's `src/test/resources` directory.

You can name the S3 bucket whatever you want (and change its system property to match), but make sure the bucket is only used for these integration tests. The tests will delete all the contents of the bucket as a part of the test tear down.

Lastly, if you don't want to build it yourself, the library can be downloaded from the Maven central repository by putting the following in your project's [pom.xml file](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html):

    <dependency>
      <groupId>info.freelibrary</groupId>
      <artifactId>vertx-pairtree</artifactId>
      <version>${vertx.pairtree.version}</version>
    </dependency>

_Actually, this is not true yet because I haven't published a version to the central Maven repository, but it will be soon._

### Acknowledgements

This library incorporates code from the [SuperS3t](https://github.com/spartango/SuperS3t/) project. It's published under the same license as this project.

### Contact

If you have questions about vertx-pairtree feel free to ask them on the FreeLibrary Projects [mailing list](https://groups.google.com/forum/#!forum/freelibrary-projects); or, if you encounter a problem, please feel free to [open an issue](https://github.com/ksclarke/vertx-pairtree/issues "GitHub Issue Queue") in the project's issue queue.
