# vertx-pairtree &nbsp;[![Build Status](https://api.travis-ci.org/ksclarke/vertx-pairtree.svg)](https://travis-ci.org/ksclarke/vertx-pairtree) [![Codacy Badge](https://api.codacy.com/project/badge/Coverage/ebf45038ace1469e842989f8d860ed1c)](https://www.codacy.com/app/ksclarke/vertx-pairtree?utm_source=github.com&utm_medium=referral&utm_content=ksclarke/vertx-pairtree&utm_campaign=Badge_Coverage) [![Known Vulnerabilities](https://snyk.io/test/github/ksclarke/vertx-pairtree/badge.svg)](https://snyk.io/test/github/ksclarke/vertx-pairtree) [![Maven](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/info/freelibrary/vertx-pairtree/maven-metadata.xml.svg?colorB=brightgreen)](http://mvnrepository.com/artifact/info.freelibrary/vertx-pairtree) [![Javadocs](http://javadoc.io/badge/info.freelibrary/vertx-pairtree.svg)](http://projects.freelibrary.info/vertx-pairtree/javadocs.html)

A file system or S3 backed [Pairtree](https://wiki.ucop.edu/display/Curation/PairTree) library for use with the [Vert.x](http://vertx.io/) tool-kit. To use it, include its Jar file in your project's classpath.

### Getting Started

To check out and build the project, type on the command line:

    git clone https://github.com/ksclarke/vertx-pairtree.git
    cd vertx-pairtree
    mvn install

This will run the build and the unit tests for the file system back end. Right now, the only tests for the S3 back end are integration tests. To run a build with them, you will need to put the following properties in your [settings.xml file](https://maven.apache.org/settings.html) and run the build with the `s3_it` profile:

    <vertx.pairtree.bucket>YOUR_S3_BUCKET_NAME</vertx.pairtree.bucket>
    <vertx.pairtree.access_key>YOUR_ACCESS_KEY</vertx.pairtree.access_key>
    <vertx.pairtree.secret_key>YOUR_SECRET_KEY</vertx.pairtree.secret_key>

Or, you can supply the required properties on the command line (with the same `s3_it` profile) when you build the project:

    mvn install -Ps3_it -Dvertx.pairtree.bucket=YOUR_S3_BUCKET_NAME \
      -Dvertx.pairtree.access_key=YOUR_ACCESS_KEY \
      -Dvertx.pairtree.secret_key=YOUR_SECRET_KEY

The YOUR_S3_BUCKET_NAME, YOUR_ACCESS_KEY and YOUR_SECRET_KEY values obviously need to be replaced with real values. Within AWS' Identity and Access Management service you can configure a user that has permission to perform actions on the S3 bucket you've created for this purpose. For an example of the IAM inline user policy for an S3 bucket, consult the [example JSON file](https://github.com/ksclarke/freelib-utils/blob/master/src/test/resources/sample-iam-policy.json) in the project's `src/test/resources` directory.

You can name the S3 bucket whatever you want (and change its system property to match), but make sure the bucket is only used for these integration tests. The tests will delete all the contents of the bucket as a part of the test tear down.

If you want to put your test S3 bucket in a region other than the standard us-east-1, you will also need to supply a `vertx.pairtree.region` argument. For example:

    mvn install -Ps3_it -Dvertx.pairtree.bucket=YOUR_S3_BUCKET_NAME \
      -Dvertx.pairtree.access_key=YOUR_ACCESS_KEY \
      -Dvertx.pairtree.secret_key=YOUR_SECRET_KEY \
      -Dvertx.pairtree.region="us-west-2"

It can also be supplied through your settings.xml file. At this point, only regions that support signature version 2 authentication are supported. To see the valid S3 region endpoints, consult [AWS' documentation](http://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region).

Lastly, if you don't want to build it yourself, the library can be downloaded from the Maven central repository by putting the following in your project's [pom.xml file](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html):

    <dependency>
      <groupId>info.freelibrary</groupId>
      <artifactId>vertx-pairtree</artifactId>
      <version>${vertx.pairtree.version}</version>
    </dependency>

### Acknowledgements

This project incorporates code from the [SuperS3t](https://github.com/spartango/SuperS3t/) project. It's published under the [same license](licenses/LICENSE-SuperS3t.txt) as this project.

This project also incorporates code from an early version of the Library of Congress' [Pairtree](https://github.com/LibraryOfCongress/pairtree) implementation. It was released into the [public domain](licenses/LICENSE-LoC.txt) because it was developed by a U.S. government agency.


### Contact

If you have questions about vertx-pairtree <a href="mailto:ksclarke@ksclarke.io">feel free to ask</a> or, if you encounter a problem, please feel free to [open a ticket](https://github.com/ksclarke/vertx-pairtree/issues "GitHub Issue Queue") in the project's issues queue.
