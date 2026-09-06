# RAHSA

![RAHSA logo](branding/logo_RAHSA.png)

RAHSA is an offline Android password manager backed by a local KDBX vault.
Credentials remain on the device; the prototype has no backend and requests no
Internet permission.

## Prototype capabilities

- Create, unlock, and explicitly lock a local KDBX vault.
- Store, edit, search, and delete credentials.
- Generate passwords and copy sensitive values with timed clipboard cleanup.
- Lock when backgrounded and protect sensitive screens from screenshots.
- Run without an account, cloud service, or network connection.

The exact retained scope and known limitations are documented in
[`PRD.md`](PRD.md), [`TASKS.md`](TASKS.md), and
[`docs/TEST_RESULTS.md`](docs/TEST_RESULTS.md).

## Build

Prerequisites:

- JDK 17
- Android SDK compatible with compile/target SDK 35
- Android SDK platform tools when installing or testing through ADB

Build the current free debug prototype from the repository root:

```powershell
.\gradlew.bat :app:assembleFreeDebug
```

The APK is produced under `app/build/outputs/apk/free/debug/`.

## Brand status

RAHSA is the selected final product name and the supplied shield logo is the
approved identity asset. The Android package/application ID and release signing
identity remain intentionally unchanged until their owner-controlled values are
approved. Trademark screening, complete dependency notices, and store identity
must be completed before public distribution; see
[`docs/BRAND_DECISION.md`](docs/BRAND_DECISION.md).

## Open-source attribution

RAHSA is derived from
[`yogeshpaliyal/KeyPass`](https://github.com/yogeshpaliyal/KeyPass) and uses
[`keemobile/kotpass`](https://github.com/keemobile/kotpass). Both projects are
MIT-licensed. Copyright notices and attribution details are preserved in
[`LICENSE`](LICENSE), [`NOTICE.md`](NOTICE.md), and
[`licenses/KOTPASS_LICENSE.txt`](licenses/KOTPASS_LICENSE.txt).
