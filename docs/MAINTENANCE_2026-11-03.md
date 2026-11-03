# Zeppelin Must Have 0.17.1 — dependency and runtime verification

Date: 2026-11-03
Status: **JAR built and client launched; dedicated server / GameTests blocked by a dependency defect.**
Target: Minecraft 1.21.1, Java 21, NeoForge 21.1.265.

## Dependency version audit

| Dependency | Old | Selected | Upstream check |
| --- | --- | --- | --- |
| ModDevGradle | 2.0.141 | 2.0.148 | Resolved by Gradle |
| NeoForge | 21.1.235 | 21.1.265 | Latest 21.1.x in repository metadata |
| Create | 6.0.10 | 6.0.10 | Latest 1.21.1 stable release |
| Simulated | 1.3.0 | 1.3.2 | Latest in upstream Maven metadata |
| Aeronautics | 1.3.0 | 1.3.2 | Latest in upstream Maven metadata |
| Sable | 2.0.0 | 2.0.6 | Latest in upstream Maven metadata |
| Curios API | 9.2.2+1.21.1 | 9.5.1+1.21.1 | Resolved by Gradle |
| Flywheel | 1.0.6 | 1.0.6 | Same version bundled in stable Create |
| Ponder | 1.0.82 | 1.0.82 | Same version bundled in stable Create |

Version metadata now declares Zeppelin Must Have **0.17.1**. All registry IDs,
public deferred handles, NBT keys, and data-pack profile locations are unchanged.

## Maven TLS incident and secure workaround

On 2026-11-03, the repository `maven.createmod.net` at 138.201.22.210
presented a certificate for `CN=*.createmod.net` issued by Let's Encrypt
YR1, which expired at **2026-11-03 10:23:28** local time. Windows curl
reported `SEC_E_CERT_EXPIRED`; Gradle Java reported
`PKIX path building failed`. Normal Gradle resolution could not download
Create, Ponder, or Flywheel.

No TLS or hostname verification was disabled, and no expired certificate was
trusted. Instead, the official Create 6.0.10 release was downloaded over
verified HTTPS from Modrinth (project `LNytGWDc`) and checked against the
published SHA-512:

```
11cc8fc049d2f67f6548c7abfada6b82a3adb5c7ca410a742de04bbca76e03862c518721b88d806f6e6d768a4d68531fdb903a85859b25d1484d550cc7bafd4b
```

That official release contains Ponder 1.0.82, Flywheel 1.0.6, and Registrate.
An opt-in temporary Maven repository maps these checked release files to
the development coordinates. Flywheel's bundled runtime JAR also contains
the required Flywheel API classes. The local POM files are synthesized
*only for development resolution* and are not distributed.

The persistent, reproducible preparation scripts are:

- `scripts/prepare-verified-create.ps1`: retrieve, SHA-512-verify,
  extract, and stage artifacts under the ignored
  `.gradle/zeppelin-verified-create/` directory.
- `scripts/verified-create.init.gradle`: opt-in local dependency resolution.
- `scripts/verify-release.ps1`: full build, GameTest checks, and JAR hash.
  Specify `-UseVerifiedCreateBundle` when the upstream TLS issue persists.

## Confirmed results

- Gradle `compileJava`: **PASS** using the checked Create release.
- Gradle `build` including `compileTestJava`, `test`, `check`,
  `jar`, `sourcesJar`: **PASS**.
- All **3 JUnit tests PASS**.
- Generated `neoforge.mods.toml` reports version **0.17.1**.
- JAR path: `build/libs/zeppelin_must_have-0.17.1.jar`.
- JAR size: **1,044,417 bytes**.
- SHA-256:
  `C847FF24CB07A995FDC544029EAE0149F0F2543CD5A8BD98BB030F954A62E3CA`.
- Client development run **started successfully** using the checked
  dependency set: the Minecraft 1.21.1 integrated server started at
  18:27:07 and the `Dev` player joined the world at 18:27:08.
  Zeppelin Must Have data-pack profiles were loaded successfully.
- Windows narrator emitted `SP_VOICE` initialization warnings,
  but the game progressed into the world.

## Outstanding dedicated-server failure

`runGameTestServer` reached NeoForge mod construction and failed in
the external Create Simulated **1.3.2** dependency:

```
Create Simulated (simulated) has failed to load correctly
Attempted to load class net/minecraft/client/gui/screens/Screen
for invalid dist DEDICATED_SERVER
at dev.simulated_team.simulated.index.SimBlocks.register(SimBlocks.java:939)
```

**The GameTests were not executed**. The Gradle run task itself returned
`BUILD SUCCESSFUL` despite the Minecraft crash, which is not a valid
game-test pass. An independent GameTest log assertion is required before
publishing. This failure occurs in the dependency before the Zeppelin
Must Have mod is constructed; do not claim that the dedicated-server
configuration is functional until the upstream issue is resolved.

The dedicated `runServer` was previously blocked in offline mode before
the fallback was prepared; it has not been independently confirmed to
start successfully. The failing GameTest server is direct evidence of a
server-side blocker in the current mod stack.

## How to reproduce the successful build

From the repository root, run:

```powershell
powershell.exe -NoProfile -ExecutionPolicy RemoteSigned -File .\scripts\verify-release.ps1 -Stage build -UseVerifiedCreateBundle
```

For client run and dedicated server testing, use `-Stage client` and
`-Stage server` respectively, with `-UseVerifiedCreateBundle`.
Use `-Stage full` only to attempt the strict GameTest release gate;
it must fail until a successful GameTest result is confirmed.
Server EULA acceptance is a separate manual action.

Do not advertise v0.17.1 as a fully server-verified release. Repeat
client and dedicated-server smoke tests, GameTests, Sable native loading,
world saves, vessel control, and emergency-stop behavior before publication.
