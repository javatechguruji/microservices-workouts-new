# Start, connect, and stop the existing AWS VM

Run these commands from this workspace's root. AWS CLI, Terraform, and SSH must already
be installed. The scripts use your existing `workouts-dev` AWS profile, Terraform state,
and SSH key in the sibling `microservices-workouts` workspace. Keep that workspace in
place. No credentials, state, or keys are copied here.

## 1. Select the profile and log in

```bash
export AWS_PROFILE=workouts-dev
aws sso login --profile "$AWS_PROFILE"
aws sts get-caller-identity --profile "$AWS_PROFILE"
```

Check that the displayed account is the one containing your existing VM. Your previous
workspace uses SSO login. To list configured profiles:

```bash
aws configure list-profiles
aws configure list --profile workouts-dev
```

Only if the profile is missing, configure it using your existing organization's SSO
start URL, SSO region, account, and role, then repeat login:

```bash
aws configure sso --profile workouts-dev
```

If the original workspace is no longer beside this workspace, set its actual path in
each terminal before running a script:

```bash
export REFERENCE_WORKSPACE="/Users/haneefnoorbasha/Workouts/microservices-workouts"
```

## 2. Start the VM and existing services

```bash
bash aws-infra/start.sh
```

This reads the existing instance ID and region from Terraform outputs, starts EC2,
waits for readiness, and starts the existing remote Compose services and Minikube if
installed. It preserves data and does not provision resources or install software.
The existing remote workspace and its infrastructure helpers must still be present.

## 3. Connect from your laptop

```bash
bash aws-infra/connect.sh
```

Leave this terminal open. The tunnel forwards infrastructure ports to AWS:

| Tool | Local address |
| --- | --- |
| Collector OTLP HTTP | `http://localhost:4318` |
| Collector OTLP gRPC | `localhost:4317` |
| Grafana | `http://localhost:3000` |
| Prometheus | `http://localhost:9090` |
| Loki | `http://localhost:3100` |
| Tempo | `http://localhost:3200` |
| Collector health | `http://localhost:13133` |

It also forwards the original database, cache, authentication, messaging, and alerting
ports. It does not forward backend application ports, leaving 9101 and 9105 free for
your IntelliJ applications. Use the Java agent options in
[S030](../docs/observability/s030_logging.md#how-order-service-enables-the-java-agent).

Press **Ctrl+C** to close the tunnel; this does not stop the VM. If forwarding fails
because a port is occupied, close the previous tunnel or the process using that port
and retry. Do not run the old and new tunnels together.

## 4. Stop for the day

Stop your IntelliJ applications and close the tunnel with Ctrl+C. Then run:

```bash
export AWS_PROFILE=workouts-dev
bash aws-infra/stop.sh
```

If your AWS session has expired, repeat `aws sso login --profile workouts-dev` first.
The script shuts down remote Minikube and Compose before stopping EC2. If remote
shutdown fails, it stops with an error rather than forcing EC2 off. Data is retained;
EBS storage charges continue while the VM is stopped.

The VM's public IP is discovered each time, so no IP editing is needed. If SSH times
out after your laptop's public IP changes, the existing AWS SSH access rule may need
to be updated through your original infrastructure setup.
