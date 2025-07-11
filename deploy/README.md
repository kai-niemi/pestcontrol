# Cloud Deployment

Deployment scripts using [roachprod](https://github.com/cockroachdb/cockroach/tree/master/pkg/cmd/roachprod) 
which is a Cockroach Labs internal tool for quickly staging AWS/GCE/Azure clusters.

These scripts will:

- Create VMs
- Stage CockroachDB binaries
- Stage pestcontrol binaries
- Start pestcontrol