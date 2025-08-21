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
* [Tutorials](#tutorials)
  * [Insecure 3-node self-hosted cluster (default)](#insecure-3-node-self-hosted-cluster-default)
  * [Secure 3-node self-hosted cluster](#secure-3-node-self-hosted-cluster-)
  * [Remarks](#remarks)
* [Appendix: Configuration Files](#appendix-configuration-files)
<!-- TOC -->

# About

<img  align="left" src=".github/logo.png" alt="" width="64"/> 

[Pest Control](https://github.com/kai-niemi/pestcontrol) is a sandbox tool for managing and chaos testing 
CockroachDB clusters. It provides both a graphical and command-line 
interface for controlling and visualizing CockroachDB node failure
and recovery and it's impact on application workloads. It supports 
CockroachDB Cloud and self-hosted clusters, for which it also provides 
easy-to-use operator scripts.

## Main features

The main features include:

- Install and bootstrap self-hosted CockroachDB clusters locally or in a network environment.
- Visualize cluster topologies and node health.
- Run synthetic client workloads and visualize impact during steady state and adverse events.
- Provide CockroachDB Cloud disruption API controls for chaos testing.
- Integrate with [Toxiproxy](https://github.com/Shopify/toxiproxy) for self-hosted chaos testing.

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

Pest Control consists of these parts:

- A single spring boot app with:
  - A Web UI API for visualizing cluster layouts, workload graphs and metrics.
  - A REST API for installing and managing self-hosted CockroachDB clusters.
  - A shell for managing and chaos testing clusters using bash scripts, cloud API or toxiproxy.
- Bash scripts for installing and managing self-hosetd clusters.

All these parts are bundled into a single `tar.gz` assembly at build time. Depending on
the target cluster type, there are a few different usage modes.

### CockroachDB Cloud

In this mode, you simply start the app and connect to it with a web browser. Then you
can login to a pre-configured set of existing cloud clusters. To chaos test a cloud cluster, 
you can use shell commands to distrupt and recover either zones or entire regions. Notice that
it requires special steps in your cluster deployment to enable this opt-in feature.

### Self-hosted Locally

tba

### Self-hosted Remotely

tba

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
  - A CockroachDB cloud cluster.
  - A local environment with one machine/instance for all nodes.
  - A network environment with one machine/instance per node. 
    - See [deploy](deploy) for `roachprod` deployments (mainly targeting CRL internal use).

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
> to enable it to intercept and trash the gRPC traffic between nodes.

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
| cluster-type           | Yes      | hosted_insecure | `cloud_serverless,cloud_standard,cloud_dedicated,hosted_insecure,hosted_secure`                                  |
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

- To manage `hosted` cluster types on dedicated machines, you also need to deploy 
pestcontrol on each host. These instances will act as gateways to run local bash scripts to 
start, stop, kill nodes etc. One instance then acts as control plan and sends HTTP requests
to the other instances when running such shell commands.
- If you switch between the `secure` and `insecure` modes, re-run the `init` command to 
set proper SQL user roles and secrets.

# Appendix: Configuration Files

Pest Control can be configured through the files available in the `config` directory:

1. [init.sql](config/init.sql) - Init SQL statements (optional).
1. [application-default.yml](config/application-default.yml) - Cluster connection settings.

---

That is all, carry on!
