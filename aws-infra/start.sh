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

case "$STATE" in
  stopping) aws_ec2 wait instance-stopped --instance-ids "$INSTANCE"; STATE=stopped ;;
  stopped|pending|running) ;;
  *) echo "Cannot start VM in state $STATE" >&2; exit 1 ;;
esac
if [[ "$STATE" == stopped ]]; then aws_ec2 start-instances --instance-ids "$INSTANCE" >/dev/null; fi
aws_ec2 wait instance-running --instance-ids "$INSTANCE"
aws_ec2 wait instance-status-ok --instance-ids "$INSTANCE"
prepare_ssh
ready=false
for attempt in {1..60}; do
  if ssh "${SSH_ARGS[@]}" "$REMOTE" true; then ready=true; break; fi
  sleep 5
done
[[ "$ready" == true ]] || { echo 'SSH unavailable. Check your key and existing SSH firewall rule.' >&2; exit 1; }
ssh "${SSH_ARGS[@]}" "$REMOTE" 'cd ~/microservices-workouts && sudo bash infra/aws-dev/compose.sh start'
ssh "${SSH_ARGS[@]}" "$REMOTE" 'cd ~/microservices-workouts && if [ -f infra/aws-dev/minikube-vm.sh ]; then sudo bash infra/aws-dev/minikube-vm.sh start; fi'
echo 'VM and existing services started. Allow services to become ready, then run bash aws-infra/connect.sh.'
