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
  * [Install Haproxy (optional)](#install-haproxy-optional)
* [Building](#building)
  * [Clone the project](#clone-the-project)
  * [Build the artifacts](#build-the-artifacts)
* [Running](#running)
* [Configuration](#configuration)
* [Usage Tutorials](#usage-tutorials)
  * [Local 3-node self-hosted cluster (insecure)](#local-3-node-self-hosted-cluster-insecure)
  * [Local 3-node self-hosted cluster (secure)](#local-3-node-self-hosted-cluster-secure)
  * [Remote 3-node self-hosted cluster (insecure)](#remote-3-node-self-hosted-cluster-insecure)
  * [Remarks](#remarks)
<!-- TOC -->

# About

<img  align="left" src="docs/logo.png" alt="" width="64"/> 

[Pest Control](https://github.com/kai-niemi/pestcontrol) is a tool for managing 
and chaos testing CockroachDB clusters. It provides a command-line shell for 
controlling and disrupting cluster deployments and a web interface for visualizing 
node failure and recovery, including the impact on synthetic application workloads. 

## Main features

- Simple local and remote self-hosted cluster management.
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
  - Disruption API requires a feature flag enabled for the organization
- MacOS (main platform)
- Linux

## How it works

Pest Control is a single spring boot application with a shell offering commands to 
installing and control CockroachDB nodes via bash scripts. In a network environment, 
it uses itself as an agent to invoke local commands on behalf of a control-plane instance.
For Cockroach Cloud clusters, it only provides disruption API controls and no web UI 
support for visualization.

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
- You can use the following cluster types:
  - Local - one machine/instance for all nodes (laptop typically).
  - Remote- A network environment with one machine/instance per node. For this type, Pest Control 
  must be running on each node acting as an agent for a control plane instance.
  - Cloud - An existing CockroachDB Cloud cluster. For this type, you can only use the disruption 
  API through shell commands.
    
Notice that Pest Control does not interact with any public cloud APIs for cluster provisioning 
and deployment. It's either local, your own pre-configured network or CockroachDB Cloud.

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

## Install Haproxy (optional)
                             
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

# Running

To start the app in the foreground with an interactive shell:

    ./pest

To start the app with the web UI enabled:

    ./pest --web

To start the app in the foreground with a non-interactive shell to run a command 
(like `status 1`) and then quit:

    ./pest node status -n 1

To start the app in the background without shell:

    ./pest-op start-service

Now you can access the application via http://localhost:9090.

To stop the app in the background:

    ./pest-op stop-service

# Configuration

Pest Control adopts convention over configuration, as far as it goes. After that, there's baseline
configuration and after that there's per-node configuration. Everything is configured through a single
YAML file like [config/application-default.yml](config/application-default.yml).

You can either edit the default profile file above directly, or create a new one with a custom name
suffix and then pass that name in the `--profiles` argument.

The active profile(s) will be listed in the startup banner and the selected cluster in shell prompt.

Example of creating a new profile:

    cp config/application-default.yml config/application-craig.yml
    java -jar pestcontrol.jar --profiles craig

For a configuration reference, see the comments added to each item below:

```yaml
application:
  # Defines which cluster to pre-select
  default-cluster-id: "local-insecure" 
  # List of all available clusters
  clusters: 
      # Unique cluster ID, the UUID in case of cockroachdb cloud
    - cluster-id: "remote-insecure"
      # Cluster title
      cluster-name: "Remote Cluster (insecure)" 
      # Defines the range of commands available to this cluster.
      # cloud_serverless
      # cloud_standard
      # cloud_dedicated
      # hosted_insecure
      # hosted_secure
      cluster-type: hosted_insecure
      # URL pointing either at the first node console or haproxy HTTP listener
      admin-url: "http://localhost:8080"
      # Database connection properties   
      data-source-properties:
        url: "jdbc:postgresql://localhost:26257/defaultdb?sslmode=disable"
        username: "craig"
        password: ""
      # Port numbers when generating haproxy config
      load-balancer:
        rpc-addr: :26250
        http-addr: :8070
        stats-addr: :7070
      # Baseline DB version and address and port tuples. The + sign denotes incremental port assignments.
      # If internal-ips is defined, each node will the prefixed with the address, thus
      # the internal-ips and nodes must be equal in size.
      baseline:
        # The CockroachDB version to download and install
        # See https://www.cockroachlabs.com/docs/releases/#downloads
        version: "v25.3.4.darwin-11.0-arm64"
        # version: "v25.3.4.darwin-11.0-amd64"
        # version: "v25.3.4.linux-amd64"
        # version: "v25.3.4.linux-arm64"
        # Pest Control base URL
        service-addr: localhost:9091
        # CockroachDB listen addr
        listen-addr: :+25257
        # CockroachDB advertise addr
        advertise-addr: localhost
        # CockroachDB advertise proxy addr
        advertise-proxy-addr: localhost:+35257
        # CockroachDB SQL addr
        sql-addr: :+26257
        # CockroachDB HTTP addr
        http-addr: :+8080
        # Used for remote cluster types only to replace the canonical host names
        internal-ips:
          - 10.1.2.3
          - 10.1.2.4
          - 10.1.2.5
      # List of nodes in the cluster
      nodes:
        # Required property denoting the locality flag. All the CockroachDB address:port properties
        # in the baseline section above can be overridden here.
        - locality: "region=eu-central-1,zone=eu-central-1a"
        - locality: "region=eu-central-1,zone=eu-central-1b"
        - locality: "region=eu-central-1,zone=eu-central-1c"
```

# Tutorials

## Local 3-node self-hosted cluster (insecure)

Start the interactive shell with:

    ./pest

The commands will download and install the CockroachDB binaries, start a local insecure 
3-node cluster, initialize the cluster and also start haproxy.
    
````shell
node install
node start -n 1-3
node init 
haproxy gen
haproxy start
exit
````

You can also copy all of the above to a text file and use:

    ./pest @cmd.txt

## Local 3-node self-hosted cluster (secure)

These commands will download and install the CockroachDB binaries, start a local secure 
3-node cluster with haproxy and initialize the cluster.

```shell
cluster use --clusterId local-secure
node install
node certs
node start -n 1-3
node init 
haproxy gen
haproxy start
exit
```

You can also copy all of the above to a text file and use:

    ./pest @cmd.txt

The secure mode uses self-signed CA certificates and keys stored in the `.certs` directory, 
including a PKCS12 truststore used by the web app. To login to a secure cluster, you may 
need to restart the shell in order to pick up the self-signed certificate.

## Remote 3-node self-hosted cluster (insecure)

To deploy and manage a cluster on dedicated machines, you first need to deploy and run 
pestcontrol agents on each host. These agents will act as gateways to run local bash scripts 
to start, stop, kill nodes and so on. Your local instance will act as the control plane and 
send HTTP requests to the other instances when running shell commands like `start`.

A quick method is to scp the tar.gz assembly to each host. 
Assuming you have 3 pest control machines, host1, host2 and host3:

```shell
scp target/pestcontrol-2.0.0-bin.tar.gz user@host1:/~
scp target/pestcontrol-2.0.0-bin.tar.gz user@host2:/~
scp target/pestcontrol-2.0.0-bin.tar.gz user@host3:/~
ssh -t user@host1 'tar xvf pestcontrol-2.0.0-bin.tar.gz && cd pestcontrol-2.0.0 && ./pest-op start-service'
ssh -t user@host2 'tar xvf pestcontrol-2.0.0-bin.tar.gz && cd pestcontrol-2.0.0 && ./pest-op start-service'
ssh -t user@host3 'tar xvf pestcontrol-2.0.0-bin.tar.gz && cd pestcontrol-2.0.0 && ./pest-op start-service'
```

On the control host, your local laptop/desktop for example, a cluster configuration named `remote-insecure` 
with the IP/host names setup accordingly (no need to sync it across machines):

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
        version: "v25.3.4.darwin-11.0-arm64"
        service-addr: :9091
        listen-addr: :+25257
        advertise-addr: :+25257
        advertise-proxy-addr: :+35257
        sql-addr: :+26257
        http-addr: :+8080
        internal-ips:
          - 10.1.2.3
          - 10.1.2.4
          - 10.1.2.5
      nodes:
        - locality: "region=eu-central-1,zone=eu-central-1a"
        - locality: "region=eu-central-1,zone=eu-central-1b"
        - locality: "region=eu-central-1,zone=eu-central-1c"
```

On the control host, start the interactive shell with:

    ./pest --cluster remote-insecure
    
Then execute:

```shell
node install -n 1-3
node start -n 1-3
node init
```

## Remarks

- If you switch between the `secure` and `insecure` modes, re-run the `init` command to
  set proper SQL user roles and secrets.

---

That is all, carry on!
