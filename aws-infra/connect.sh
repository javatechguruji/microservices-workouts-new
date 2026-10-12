#!/usr/bin/env bash
# Use the existing deployment and key; do not provision or delete resources.
set -euo pipefail
BASE=$(cd "$(dirname "$0")" && pwd)
REFERENCE_WORKSPACE=${REFERENCE_WORKSPACE:-"$(dirname "$(dirname "$BASE")")/microservices-workouts"}
TF="$REFERENCE_WORKSPACE/infra/aws-dev/terraform"
KEY="$REFERENCE_WORKSPACE/infra/aws-dev/.ssh/id_ed25519"
export AWS_PROFILE=${AWS_PROFILE:-workouts-dev}
export AWS_PAGER=""
[[ $# == 0 ]] || { echo "This script takes no arguments." >&2; exit 1; }
for tool in aws terraform ssh; do
  command -v "$tool" >/dev/null || { echo "Missing command: $tool" >&2; exit 1; }
done
[[ -d "$TF" && -f "$KEY" ]] || { echo "Existing Terraform directory or SSH key missing under $REFERENCE_WORKSPACE" >&2; exit 1; }
INSTANCE=$(terraform -chdir="$TF" output -raw instance_id)
REGION=$(terraform -chdir="$TF" output -raw aws_region)
[[ -n "$INSTANCE" && -n "$REGION" ]] || { echo 'Existing deployment outputs are missing.' >&2; exit 1; }
aws_ec2() { aws ec2 "$@" --region "$REGION"; }
STATE=$(aws_ec2 describe-instances --instance-ids "$INSTANCE" --query 'Reservations[0].Instances[0].State.Name' --output text)
prepare_ssh() {
  local ip
  ip=$(aws_ec2 describe-instances --instance-ids "$INSTANCE" --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)
  [[ -n "$ip" && "$ip" != None && "$ip" != null ]] || { echo 'VM has no public IP.' >&2; return 1; }
  SSH_ARGS=(-F /dev/null -i "$KEY" -o IdentitiesOnly=yes -o BatchMode=yes
    -o "HostKeyAlias=$INSTANCE" -o StrictHostKeyChecking=accept-new
    -o ConnectTimeout=10 -o ServerAliveInterval=30 -o ServerAliveCountMax=3)
  REMOTE="ubuntu@$ip"
}

[[ "$STATE" == running ]] || { echo 'Run bash aws-infra/start.sh first.' >&2; exit 1; }
prepare_ssh
FORWARDS=()
# Infrastructure only: leave local application ports 9101 and 9105 free.
for port in 5432 6379 8180 9092 8089 4317 4318 3000 9090 9093 3100 3200 13133; do
  FORWARDS+=(-L "127.0.0.1:$port:127.0.0.1:$port")
done
echo 'Tunnel open until Ctrl+C. Collector: localhost:4318; Grafana: http://localhost:3000'
exec ssh "${SSH_ARGS[@]}" -o ExitOnForwardFailure=yes -N -T "${FORWARDS[@]}" "$REMOTE"
