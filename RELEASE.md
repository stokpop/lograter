# Publishing LogRater

## Prerequisites

Configure these secrets in *Settings → Secrets and variables → Actions*:

| Secret | Description |
|---|---|
| `MAVEN_CENTRAL_USERNAME` | Central Portal user token username (generate at [central.sonatype.com](https://central.sonatype.com) → Account → User Token) |
| `MAVEN_CENTRAL_PASSWORD` | Central Portal user token password |
| `GPG_SIGNING_KEY` | Armored PGP private key (`gpg --export-secret-keys --armor <key-id>`) |
| `GPG_SIGNING_PASSWORD` | GPG passphrase |

Allow the release workflow to push back to `master` after publishing (branch protection → add `github-actions[bot]` to the bypass list).

## Steps to release

1. **Create a GitHub release** — go to *Releases → Draft a new release*, create a tag (e.g. `1.5.6`), and click **Publish release**.
2. The [release workflow](.github/workflows/publish-maven-central.yml) triggers automatically: signs and uploads the artifact, bumps `gradle.properties` to the next `-SNAPSHOT`, and attaches JARs to the release.
3. **Approve on Maven Central** — go to [central.sonatype.com/publishing/deployments](https://central.sonatype.com/publishing/deployments) and click **Publish**.

> Maven Central releases are irreversible — verify the deployment before publishing.

## Snapshot releases

Trigger the workflow manually via *Actions → Release to Maven Central → Run workflow* and enter a version ending in `-SNAPSHOT` (e.g. `1.5.7-SNAPSHOT`). Snapshots publish automatically without a manual approval step.
