#!/bin/bash
set -e

# Match the docker socket's group, whatever the host uses
SOCK=/var/run/docker.sock
if [ -S "$SOCK" ]; then
  GID=$(stat -c '%g' "$SOCK")
  if [ "$GID" != "0" ]; then
    getent group "$GID" >/dev/null || groupadd -g "$GID" dockerhost
    usermod -aG "$(getent group "$GID" | cut -d: -f1)" jenkins
  fi
fi

# Fix volume ownership if needed (replaces the manual chown)
[ "$(stat -c '%u' /var/jenkins_home)" = "1000" ] || chown -R jenkins:jenkins /var/jenkins_home

# Drop root and start Jenkins as the jenkins user
export HOME=/var/jenkins_home
exec setpriv --reuid=jenkins --regid=jenkins --init-groups \
  /usr/bin/tini -- /usr/local/bin/jenkins.sh "$@"