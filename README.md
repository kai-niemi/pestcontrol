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
* [Configuration](#configuration)
* [Running](#running)
* [Tutorials](#tutorials)
  * [Local 3-node self-hosted cluster (insecure)](#local-3-node-self-hosted-cluster-insecure)
  * [Local 3-node self-hosted cluster (secure)](#local-3-node-self-hosted-cluster-secure)
  * [Remote 3-node self-hosted cluster (insecure)](#remote-3-node-self-hosted-cluster-insecure)
* [Remarks](#remarks)
<!-- TOC -->

# About

<img  align="left" src=".github/logo.png" alt="" width="64"/> 

[Pest Control](https://github.com/kai-niemi/pestcontrol) is a tool for managing 
and chaos testing CockroachDB clusters. It provides a command-line interface for 
controlling cluster deployments and a web interface for visualizing CockroachDB 
node failure and recovery, including impact on synthetic application workloads. 

## Main features

- Simple self-hosted cluster management.
- Synthetic workloads to visualize impact during adverse events.
- Disruption API controls for chaos testing of Cockroach Cloud clusters.
- [Toxiproxy](https://github.com/Shopify/toxiproxy) integration for network level chaos testing of self-hosted clusters.

![ui1](.github/ui-1.png)

![ui2](.github/ui-2.png)

## Compatibility

Supported platforms and versions:

- CockroachDB Local Self-Hosted v22.2+
  - Secure or insecure mode
- CockroachDB Cloud v22.2+
  - Disruption API requires a feature flag enabled for the organization (submit a support request)
- MacOS (main platform)
- Linux

## How it works

Pest Control is a single spring boot app with shell scripts for installing and controlling 
CockroachDB nodes. In a network environment, it uses itself as an agent to invoke local 
commands on behalf of a controlling instance. For Cockroach Cloud clusters, it only provides 
disruption API controls and no visualization.

# Terms of Use

This tool is not supported by Cockroach Labs. Use of this tool is entirely at your
own risk and Cockroach Labs makes no guarantees or warranties about its operation.

See [MIT](LICENSE.txt) for terms and conditions.

# Prerequisites

Things you need to run Pest Control.

- Java 21+ JDK
  - https://openjdk.org/projects/jdk/21/
  - https://www.oracle.com/java/technologies/downloads/#java21
- Toxiproxy (optional)
  - https://github.com/Shopify/toxiproxy
- Depending on cluster configuration, you can either use:
  - An existing CockroachDB cloud cluster.
  - A local environment with one machine/instance for all nodes.
  - A network environment with one machine/instance per node. In this mode, pest control 
  must be running on each node acting as an agent.
    
Notice that pest control does not interact with any cloud API for cluster provisioning.

## Install the JDK

MacOS (using sdkman):

    curl -s "https://get.sdkman.io" | bash
    sdk list java
    sdk install java 21.0 (pick version)  

Ubuntu:

    sudo apt-get install openjdk-21-jdk

## Install Toxiproxy (optional)

Toxiproxy is a TCP/IP interceptor chaos testing tool that can be used with pest control 
to intercepting CockroachDB inter-node gRPC traffic. It can apply different "toxics" like 
slowing down responses, limiting bandwidth etc.

See [Installing Toxiproxy](https://github.com/Shopify/toxiproxy?tab=readme-ov-file#1-installing-toxiproxy)

# Building

Instructions for building the project locally, as an alternative to using the packaged `TAR.GZ` assembly artifact.

## Clone the project

    git clone git@github.com:kai-niemi/pestcontrol.git && cd pestcontrol

## Build the artifacts

    chmod +x mvnw
    ./mvnw clean install

# Configuration

Pest Control is configured through YAML profile files like [config/application-default.yml](config/application-default.yml). 
You can either edit the default profile directly, or create a new one with a custom name suffix 
and then pass that name in the `--profiles` argument. 

Example:

    cp config/application.yml config/application-craig.yml
    java -jar pestcontrol.jar --profiles craig

The active profile(s) will be listed in the startup banner.

# Running

Start the app in the foreground:

    ./pop run <args>

(Alt) Start the app in the background:

    ./pop start-service

Now you can access the application via http://localhost:9090.

# Tutorials

## Local 3-node self-hosted cluster (insecure)

Start the interactive shell with:

    ./pop run

The commands will download and install the CockroachDB binaries, start a local insecure 
3-node cluster with haproxy and initialize the cluster.

     install
     start 1-3
     init 
     gen-haproxy
     start-haproxy

## Local 3-node self-hosted cluster (secure)

Start the interactive shell with:

    ./pop run

The commands will download and install the CockroachDB binaries, start a local secure 
3-node cluster with haproxy and initialize the cluster.

     use --clusterId local-secure
     install
     certs
     start 1-3
     init
     gen-haproxy
     start-haproxy

The secure mode will use self-signed CA certificates and keys in `.certs` including
the PKCS12 truststore used by the web app. To login to the cluster, you need to restart 
the interactive shell for it to pick up the self-signed certificate.

## Remote 3-node self-hosted cluster (insecure)

To deploy and manage a cluster on dedicated machines, you first need to deploy and run 
pestcontrol agents on each host. These agents will act as gateways to run local bash scripts 
to start, stop, kill nodes and so on. Your local instance will act as the control plane and 
send HTTP requests to the other instances when running shell commands like `start`.

A quick method is to scp the tar.gz assembly to each host:

    scp target/pestcontrol.tar.gz user@host:/~
    ssh -t user@host 'tar xvf pestcontrol.tar.gz && cd pestcontrol && ./pop start-service'

Now, assuming you have 3 pest control instances running on 3 separate machines and a 
cluster configuration named `remote-insecure` with the IP/host names setup accordingly.

On the control host, start the interactive shell with:

    ./pop run

This will download and install the CockroachDB binaries, start a 3-node cluster and initialize it.

     use-cluster --clusterId remote-insecure     
     install 1-3
     start 1-3
     init 1

# Remarks

- If you switch between the `secure` and `insecure` modes, re-run the `init` command to 
set proper SQL user roles and secrets.

---

That is all, carry on!
