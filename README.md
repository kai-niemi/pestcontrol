<p>	
	<a href="https://github.com/kai-niemi/pestcontrol/actions/workflows/maven.yml"><img src="https://github.com/kai-niemi/pestcontrol/actions/workflows/maven.yml/badge.svg?branch=main" alt="">
</p>

<!-- TOC -->
* [About](#about)
  * [Main features](#main-features)
  * [Compatibility](#compatibility)
  * [How it works](#how-it-works)
* [Terms of Use](#terms-of-use)
* [Prerequisites](#prerequisites)
  * [Install the JDK](#install-the-jdk)
  * [Install Toxiproxy (optional)](#install-toxiproxy-optional)
* [Building](#building)
  * [Clone the project](#clone-the-project)
  * [Build the artifacts](#build-the-artifacts)
* [Installing](#installing)
* [Configuration](#configuration)
  * [Application](#application)
  * [Clusters](#clusters)
  * [DataSource Properties](#datasource-properties)
* [Running](#running)
* [Local Cluster Management](#local-cluster-management)
  * [Local Cluster Configuration](#local-cluster-configuration)
    * [Enable Toxiproxy (optional)](#enable-toxiproxy-optional)
  * [Start cluster](#start-cluster)
    * [Insecure Mode (default)](#insecure-mode-default)
    * [Secure Mode](#secure-mode)
  * [Next Steps](#next-steps)
  * [Remarks](#remarks)
  * [Shutdown](#shutdown)
* [Appendix: Configuration Files](#appendix-configuration-files)
<!-- TOC -->

# About

<img  align="left" src=".github/logo.png" alt="" width="64"/> 

[Pest Control](https://github.com/kai-niemi/pestcontrol) is both a graphical and command-line 
tool for controlling and visualizing CockroachDB cluster failures and it's impact on application 
workloads. It supports CockroachDB Cloud and self-hosted clusters for which it provides 
easy-to-use control scripts.

## Main features

The main features include:

- Visualize cluster layout and node health. 
- Provide node disruption and recovery controls.
- Integrate with [Toxiproxy](https://github.com/Shopify/toxiproxy) for chaos testing.
- Tigger client workloads and visualize impact during steady state and node / zone / region disruptions.
- Provide easy-to-use bash scripts for local CockroachDB cluster deployment and management.

Dashboard showing cluster layout and node status:

![ui1](.github/ui-1.png)

Workload page with some activity:

![ui2](.github/ui-2.png)

## Compatibility

This tool supports the following platforms and versions:

- CockroachDB Cloud v22.2+
  - Requires a feature flag enabled for the organization (file a support request) 
- CockroachDB Local Self-Hosted v22.2+
  - Secure or insecure mode
  - No license key needed
- MacOS (main platform)
- Linux

## How it works

Pest Control consists of two parts:

- A web app for visuals with a REST API for automation.
- A spring shell script for managing clusters via bash scripts.

# Terms of Use

This tool is not supported by Cockroach Labs. Use of this tool is entirely at your
own risk and Cockroach Labs makes no guarantees or warranties about its operation.

See [MIT](LICENSE.txt) for terms and conditions.

# Prerequisites

Things you need to run Pest Control locally.

- Java 21+ JDK
  - https://openjdk.org/projects/jdk/21/
  - https://www.oracle.com/java/technologies/downloads/#java21
- Toxiproxy (optional)
  - https://github.com/Shopify/toxiproxy
- Depending on cluster configuration, you can either use:
  - A single-host, local environment
  - A CockroachDB cloud cluster
  - A provisioned self-hosted environment with one machine for each node.
  
See [deploy](deploy) for `roachprod` deployments (mainly targeting CRL internal use).

## Install the JDK

MacOS (using sdkman):

    curl -s "https://get.sdkman.io" | bash
    sdk list java
    sdk install java 21.0 (pick version)  

Ubuntu:

    sudo apt-get install openjdk-21-jdk

## Install Toxiproxy (optional)

Toxiproxy is a TCP/IP interceptor based chaos testing tool. It can optionally be used
by pestcontrol to intercepting CockroachDB inter-nnode gRPC traffic and apply "toxics"
like slowing down responses, limiting bandwidth etc.

See [Installing Toxiproxy](https://github.com/Shopify/toxiproxy?tab=readme-ov-file#1-installing-toxiproxy)

> Toxiproxy is disabled by default. See configuration section on how
> to [enable](#enable-toxiproxy-optional) it to intercept the gRPC traffic 
> between nodes.

# Building

Instructions for building the project locally, as an alternative to using the packaged `TAR.GZ` assembly artifact.

## Clone the project

    git clone git@github.com:kai-niemi/pestcontrol.git && cd pestcontrol

## Build the artifacts

    chmod +x mvnw
    ./mvnw clean install

# Installing

If you prefer to use a packaged artifact (release or snapshot) rather than building, 
see [GitHub Packages](https://github.com/orgs/cloudneutral/packages?repo_name=pestcontrol). Scroll to the latest `TAR.GZ` file and copy+paste the download URL
as described:

    curl -o pestcontrol.tar.gz <paste-url-here>
    tar xvf pestcontrol.tar.gz && cd pestcontrol

# Configuration

Pest Control is configured through [config/application-default.yml](config/application-default.yml) file. You can either 
edit that file directly or create a new one with a custom name suffix and then pass that name 
in the `--profiles` argument. 

Example:

    cp config/application.yml config/application-craig.yml
    java -jar pestcontrol.jar --profiles craig

## Application

Top-level entry in `application<-profile>.yml`.

```yaml
    application:
      clusters:
        ...
 ```

| Field Name  | Optional | Default | Description                         |
|-------------|----------|---------|-------------------------------------|
| clusters    | No       | -       | Collection of CockroachDB clusters. |

## Clusters

Collection of cluster definitions.

```yaml
    application:
      clusters:
        - cluster-id: "38e2ce4f-e9b6-43ae-a9ed-64d673e443cb"
          cluster-type: cloud_dedicated
          api-key: "..."
          admin-url: "https://admin-odin-qzx.cockroachlabs.cloud:8080"
          data-source-properties:
            ...
```

| Field Name             | Optional | Default         | Description                                                                                                                                    |
|------------------------|----------|-----------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| cluster-id             | No       | -               | Either a CockroachDB Cloud cluster ID or a unique string for a local cluster                                                                   |
| cluster-type           | Yes      | cloud_dedicated | `cloud_dedicated,local_secure,local_insecure,remote_secure,remote_insecure`                                                                    |
| api-key                | Yes      | -               | Only required for `cloud_dedicated`, see [Create API Keys](https://www.cockroachlabs.com/docs/cockroachcloud/managing-access#create-api-keys). |
| admin-url              | No       | -               | Base URL for the Cluster API which is typically the regional/local cluster load balancer endpoint.                                             |
| data-source-properties | No       | -               | Data source connection parameters.                                                                                                             |

## DataSource Properties

The JDBC datasource configuration for querying node status and running workloads.

```yaml
    application:
      clusters:
          data-source-properties:
            url: "jdbc:postgresql://localhost:26257/defaultdb?sslmode=require"
            username: "craig"
            password: "cockroach"
```

| Field Name | Optional | Default   | Description                                |
|------------|----------|-----------|--------------------------------------------|
| url        | No       | -         | The JDBC connection URL.                   |
| username   | No       | craig     | The SQL user with ADMIN role.              |
| password   | Yes      | cockroach | The SQL user password for secure clusters. |

# Running

Start the app in the background:
    
    ./pest-control start-service

Now you can access the application via http://localhost:9090 and login to the cluster of choice.

**Alternative**

Start the app in the foreground:
    
    ./pest-control run-service <args>

Equivalent to:

    ln -sf target/pestcontrol.jar pestcontrol.jar
    java -jar pestcontrol.jar <args>

# Tutorials

## Insecure 3-node self-hosted cluster (default)

Start the interactive shell with:

    ./pest-control

This will download and install the CockroachDB binaries, start a 3-node cluster and initialize it.

     install 1-3
     start 1-3
     init 1

## Secure 3-node self-hosted cluster 

Start the interactive shell with:

    ./pest-control

This will download and install the CockroachDB binaries, start a 3-node cluster and initialize it.

     install 1-3
     certs 1-3
     start 1-3
     init 1
     quit

Restart the interactive shell in secure mode:

    ./pest-control --secure

The secure mode will use self-signed CA certificates and keys in `.certs` including 
the PKCS12 truststore used by the web app.

## Remarks

If you switch between the `secure` and `insecure` modes, re-run the `init` command to 
set proper SQL user roles and secrets.

# Appendix: Configuration Files

Pest Control can be configured through the files available in the `config` directory:

1. [init.sql](config/init.sql) - Init SQL statements (optional).
1. [application-default.yml](config/application-default.yml) - Cluster connection settings.

---

That is all, carry on!
