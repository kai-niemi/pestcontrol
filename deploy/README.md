# Deployment Scripts

Optional deployment scripts using [roachprod](https://github.com/cockroachdb/cockroach/tree/master/pkg/cmd/roachprod), which is a Cockroach Labs internal 
tool for quickly staging and operating AWS/GCE/Azure clusters.

These scripts assist with the following:

- Create cloud VMs for hosting:
  - CockroachDB (on each node)
  - Pestcontrol agents (on each node)
  - Toxiproxy server (on control client/s)
  - Haproxy (on control client/s)
- Stage CockroachDB binaries 
- Stage pestcontrol binaries
- Start pestcontrol agents on nodes
