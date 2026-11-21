# Jenkins Pipeline Setup

The pipeline is defined in [`Jenkinsfile`](../Jenkinsfile) at the repository root.
It is a multi-service pipeline: the `servicesList()` function is the single place to
register a service, and change detection decides which services are tested, scanned,
built, and deployed.

## Pipeline Behavior

### Pull requests

1. Checkout source code
2. Detect changed files and changed services
3. Run unit tests for the changed services
4. Publish JUnit results
5. Run SonarQube analysis and wait for the quality gate
6. Stop without building images or deploying

### Pushes to `main`

1. Checkout source code
2. Detect changed files and changed services
3. Run unit tests for the changed services
4. Publish JUnit results
5. Run SonarQube analysis and wait for the quality gate
6. Build Docker images for the changed services
7. Smoke-test the built JVM images
8. Run E2E tests when services or E2E tests changed
9. Deploy the changed services
10. Run health checks
11. Clean up old images

Infrastructure changes to `docker-compose.yml` or `Jenkinsfile` mark all services
as changed.

## Build Parameters

| Parameter | Default | Purpose |
|---|---|---|
| `AGENT_LABEL` | `built-in` | Jenkins agent label. Set to a remote agent label for distributed builds. |
| `NOTIFY_EMAIL` | `devops@buymesh.local` | Recipient for success/failure email notifications. |
| `RUN_E2E` | `true` | Run E2E tests on main-branch builds. |
| `DEPLOY_ENABLED` | `true` | Deploy changed services on main-branch builds. |

## Jenkins Requirements

### Job type

Use a **Multibranch Pipeline** or **GitHub Branch Source** job. The pipeline uses
Jenkins branch metadata such as `BRANCH_NAME`, `CHANGE_ID`, and `CHANGE_TARGET`.

The job must build from:

```text
https://github.com/rachid-serraf/BuyMesh.git
```

and use the `Jenkinsfile` in the repository root.

### Recommended plugins

- Pipeline
- Git
- GitHub Branch Source
- JUnit
- Workspace Cleanup
- Timestamper
- SonarQube Scanner
- Email Extension

### Agent requirements

The Jenkins agent must have:

- Git
- Docker CLI
- Docker Compose v2 (`docker compose`)
- Java 21 for the Maven wrapper
- Node.js 20 and npm for the frontend
- Chromium for Angular headless tests
- Permission to access the Docker socket

The provided management image installs Docker CLI, Docker Compose, Node.js, and
Chromium. The Maven wrapper downloads Maven automatically.

### Jenkins configuration

The following items are configured in Jenkins, not in this repository:

- SonarQube server named `SonarQube-Server`
- Global tool named `sonar-scanner`
- File credential named `APP_ENV_FILE`
- SMTP settings for `emailext`
- Agent labels, if using distributed builds

`APP_ENV_FILE` must contain the runtime `.env` used by Docker Compose, including
`SPRING_PROFILES_ACTIVE`, JWT keys, database credentials, Kafka settings, and the
internal API key. Do not commit that file.

## Running Jenkins Locally

```bash
cp management/.env.example management/.env
# Set SONAR_DB_PASSWORD and DOCKER_GID in management/.env
docker compose -f management/docker-compose.mgmt.yml --project-name buymesh-mgmt up -d --build
```

Find the host Docker group ID with:

```bash
getent group docker | cut -d: -f3
```

Set `DOCKER_GID` in `management/.env` to that value. Jenkins runs as the `jenkins`
user and is added to the Docker group so it can use the mounted Docker socket.

Jenkins is bound to `127.0.0.1:8084` and SonarQube to `127.0.0.1:9000`. Use an SSH
tunnel or a reverse proxy with authentication if remote access is required.

If the `jenkins-data` volume was previously created while Jenkins ran as root, fix
its ownership once:

```bash
docker run --rm -v buymesh-mgmt_jenkins-data:/var/jenkins_home \
  eclipse-temurin:21-jre-alpine chown -R 1000:1000 /var/jenkins_home
```

## GitHub Webhook

To trigger Jenkins automatically:

1. Open the GitHub repository settings.
2. Go to **Webhooks** and add a webhook.
3. Use `http://<jenkins-url>/github-webhook/` as the payload URL.
4. Enable **push** and **pull request** events.
5. Ensure GitHub can reach Jenkins through any reverse proxy or firewall.

## Test Reports

The pipeline publishes:

- Backend Maven Surefire reports from `backend/*/target/surefire-reports/*.xml`
- Frontend Karma JUnit reports from `frontend/test-results/*.xml`
- E2E Surefire reports from `backend/e2e-tests/target/surefire-reports/*.xml`

E2E Compose logs are archived as `e2e-compose.log`. Jenkins keeps the last 10 builds.

## Deployment and Rollback

Deployment uses the `APP_ENV_FILE` credential, snapshots the current image for each
service in `.prev_<service>`, and recreates only the changed services with Docker
Compose.

If deployment or the health check fails, the pipeline restores the previous image
for each service and recreates the containers. Rollback is image-level only; database
migrations and external state are not rolled back automatically.

## Notifications

The pipeline sends an email on success and failure to `NOTIFY_EMAIL`. Failure
notifications include the requestor, developers, and culprits. The email is sent
inside `catchError` so a notification problem does not change the build result.

## Security Notes

- Jenkins runs as the unprivileged `jenkins` user, not root.
- The Jenkins container is not privileged; it uses the mounted Docker socket.
- Jenkins and SonarQube bind to localhost by default.
- Runtime secrets are provided through the `APP_ENV_FILE` Jenkins credential.
- `.env` files are ignored by Git. The example files contain placeholders only.
- Configure Jenkins authorization and role-based access control for shared
  environments; those settings are not stored in this repository.

## Troubleshooting

### Pipeline waits for an agent

Check the `AGENT_LABEL` parameter. The default is `built-in`. For a remote agent,
use its configured label.

### `Could not find credentials entry with ID ...`

Create the `APP_ENV_FILE` file credential in Jenkins, or update the credential ID
in the Jenkinsfile.

### SonarQube quality gate never returns

Check the SonarQube server configuration named `SonarQube-Server`, the
`sonar-scanner` global tool, and that Jenkins can reach SonarQube.

### Docker permission denied

Confirm the `DOCKER_GID` in `management/.env` matches the host Docker group, then
recreate the Jenkins container.

### E2E tests are skipped

E2E tests run only on `main` when `RUN_E2E` is true and either a service changed or
a file under `backend/e2e-tests/` changed.
