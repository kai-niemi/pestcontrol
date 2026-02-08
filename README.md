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
  * [Install HAProxy (optional)](#install-haproxy-optional)
* [Building](#building)
  * [Clone the project](#clone-the-project)
  * [Build the artifacts](#build-the-artifacts)
* [Configuration](#configuration)
* [Tutorials](#tutorials)
  * [Local 3-node self-hosted cluster (insecure)](#local-3-node-self-hosted-cluster-insecure)
  * [Local 3-node self-hosted cluster (secure)](#local-3-node-self-hosted-cluster-secure)
  * [Remote 3-node self-hosted cluster (insecure)](#remote-3-node-self-hosted-cluster-insecure)
  * [Remarks](#remarks)
<!-- TOC -->

# About

<img  align="left" src="docs/logo.png" alt="" width="64"/> 

[Pest Control](https://github.com/kai-niemi/pestcontrol) is a tool for managing 
and chaos testing CockroachDB clusters. It provides an interactive shell for 
controlling and manipulating clusters and a web interface for visualizing 
node failure and recovery. 

## Main features

- Local and remote self-hosted cluster management.
- Network chaos testing via [Toxiproxy](https://github.com/Shopify/toxiproxy) for self-hosted clusters.
- Disruption API controls for Cockroach Cloud clusters.
- A web UI for self-hosted clusters to:
  - View cluster topology and status.
  - Run synthetic workloads to visualize application impact during adverse events.
  - Manage toxiproxy proxies and toxics.

## Compatibility

Supported platforms and versions:

- CockroachDB Self-Hosted v22.2+
  - Secure or insecure mode
  - Local or remote deployments 
- CockroachDB Cloud v22.2+
- MacOS
- Linux

## How it works

Pest Control is a single spring boot application with an embedded shell offering 
commands to install and control CockroachDB nodes via bash scripts. In a network 
environment of multiple machines, it uses itself as an agent to invoke local commands 
on behalf of a control-plane instance. There's no support for provisioning cloud
hosting environments or VMs.

# Terms of Use

This tool is not supported by Cockroach Labs. Use of this tool is entirely at your
own risk and Cockroach Labs makes no guarantees or warranties about its operation.

See [MIT](LICENSE.txt) for terms and conditions.

# Prerequisites

- Java 21+ JDK
  - https://openjdk.org/projects/jdk/21/
  - https://www.oracle.com/java/technologies/downloads/#java21
- Toxiproxy (optional)
  - https://github.com/Shopify/toxiproxy
- Haproxy (optional)
  - https://www.haproxy.org/

You can use the following CockroachDB cluster types:

  - Local - one machine/instance for all nodes (single laptop/desktop).
  - Remote - A network environment with one machine/instance per node. For this type, 
    Pest Control must be running on each node acting as an agent for the control plane instance.
  - Cloud - An existing CockroachDB Cloud cluster.

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

## Install HAProxy (optional)
                             
Usually bundled in most distributions.

# Building

Instructions for building the project locally, as an alternative to using the 
packaged `TAR.GZ` assembly artifact.

## Clone the project

    git clone git@github.com:kai-niemi/pestcontrol.git && cd pestcontrol

## Build the artifacts

    chmod +x mvnw
    ./mvnw clean install

At this point you can run it from the base directory or explode the `TAR.gz` bundle (in `target/`)
to another location.

# Configuration

Pest Control adopts convention over configuration using CockroachDB default port numbers and bindings. 
After that, there's a baseline cluster configuration and lastly per-node configuration. Everything is 
configured through a single YAML file like [config/application-default.yml](config/application-default.yml).

You can either edit the default profile file or create a new one with a custom name suffix and pass 
it in the `--profiles` argument. The active profile(s) will be listed in the startup banner and the 
selected cluster in shell prompt.

Example of creating a new profile:

    cp config/application-default.yml config/application-craig.yml
    java -jar pestcontrol.jar --profiles craig

# Tutorials

The interactive shell is started with:

    ./pest

In this mode, you can list all commands by typing `help` or pressing TAB. 
It will also start the embedded HTTP server with web dashboard available 
at http://localhost:9091.

To run in non-interactive mode, you can create a command text file and
pass the name with a `@` prefix. For example:

    ./pest @cmd.txt

## Local 3-node self-hosted cluster (insecure)

Add the following commands to a text file that will:

1) Download and install CockroachDB binaries.
2) Start a local insecure 3-node cluster.
3) Initialize the cluster.
4) Generate a haproxy config and start it.
5) Open the system browser pointing at the DB console
    
````shell
echo "install" > cmd.txt
echo "start 1-3" >> cmd.txt
echo "init" >> cmd.txt
echo "generate haproxy config" >> cmd.txt
echo "start haproxy" >> cmd.txt
echo "open dbconsole" >> cmd.txt
````

To execute all of the above, run:

````shell
./pest @cmd.txt
````

## Local 3-node self-hosted cluster (secure)

This is similar to the previous one, only it starts a secure cluster.

```shell
echo "install" > cmd.txt
echo "certs" > cmd.txt
echo "start 1-3" >> cmd.txt
echo "init" >> cmd.txt
echo "generate haproxy config" >> cmd.txt
echo "start haproxy" >> cmd.txt
echo "open dbconsole" >> cmd.txt
```

To execute all of the above, run:

````shell
./pest @cmd.txt --cluster local-secure
````

The secure mode uses self-signed CA certificates and keys stored in the `.certs` directory, 
including a PKCS12 truststore used by the web app. To login to a secure cluster, you may need 
to restart the shell in order for it to pick up the self-signed certificate for HTTP TLS traffic.

## Remote 3-node self-hosted cluster (insecure)

To deploy and manage a cluster on dedicated machines, you first need to deploy and run Pest Control 
agents on each host. These agents will act as gateways to run local bash scripts to start, stop, 
kill nodes and so on. Your local instance will act as the control plane and send HTTP requests 
to the other instances when running shell commands like `start`.

A quick method is to scp the tar.gz assembly to each host. Assuming you have 3 pest control 
machines - host1, host2 and host3:

```shell
scp target/pestcontrol-2.0.0-bin.tar.gz user@host1:/~
scp target/pestcontrol-2.0.0-bin.tar.gz user@host2:/~
scp target/pestcontrol-2.0.0-bin.tar.gz user@host3:/~
ssh -t user@host1 'tar xvf pestcontrol-2.0.0-bin.tar.gz && cd pestcontrol-2.0.0 && ./pest-op start-service'
ssh -t user@host2 'tar xvf pestcontrol-2.0.0-bin.tar.gz && cd pestcontrol-2.0.0 && ./pest-op start-service'
ssh -t user@host3 'tar xvf pestcontrol-2.0.0-bin.tar.gz && cd pestcontrol-2.0.0 && ./pest-op start-service'
```

On the control host, your local laptop/desktop for example, create a cluster configuration 
named `remote-insecure` (or anything) with the IP/host names setup accordingly. 

```yaml
    - cluster-id: "remote-insecure"
      cluster-name: "Remote Cluster (insecure)"
      cluster-type: hosted_insecure
      admin-url: "http://localhost:8080"
      data-source-properties:
        url: "jdbc:postgresql://localhost:26257/defaultdb?sslmode=disable"
        username: "craig"
        password: ""
      load-balancer:
        rpc-addr: :26257
        http-addr: :8070
        stats-addr: :7070
      baseline:
        service-addr: :9091
        listen-addr: :+25257
        advertise-addr: :+25257
        advertise-proxy-addr: :+35257
        sql-addr: :+26257
        http-addr: :+8080
        internal-ips:
          - host1
          - host2
          - host3
      nodes:
        - locality: "region=eu-central-1,zone=eu-central-1a"
        - locality: "region=eu-central-1,zone=eu-central-1b"
        - locality: "region=eu-central-1,zone=eu-central-1c"
```

On the control host, start the interactive shell with:

    ./pest --cluster remote-insecure
    
Then execute:

```shell
install all
start all
init
```

## Remarks

If you switch between the `secure` and `insecure` modes, re-run the `init` command to set proper
SQL user roles and secrets.
