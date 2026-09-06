# RAHSA Brand Decision Record

**Status:** Brand Decision Gate in progress
**Updated:** 2026-09-06

## Approved decisions

- **Product and store-facing name:** RAHSA.
- **Primary mark:** the user-supplied blue shield with white R monogram and
  padlock, stored as `branding/logo_RAHSA.png`.
- **Android presentation:** RAHSA labels, Auth/About identity, legacy launcher
  icons, adaptive foreground, and a dedicated monochrome layer are generated
  from the approved master artwork.
- **Brand colors represented by the artwork:** dominant electric blue near
  `#0068FC`, white, and the existing dark application surface.

Generated raster assets are reproducible with:

```powershell
& 'C:\Users\User\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe' scripts\generate_brand_assets.py
```

## Preliminary Indonesia trademark screening

On 2026-09-06, the official PDKI text search for `RAHSA` returned seven
records containing the word, in classes 5, 29, 30, and 32. None of those seven
normal-search records was an application/password-manager product. A phonetic
search was much broader and included similar marks, including `RAISA` in class
9.

- PDKI search: https://pdki-indonesia.dgip.go.id/search?keyword=RAHSA&type=trademark
- Relevant software screening still needs structured review in at least Nice
  classes 9 and 42, plus any class selected for the final commercial model.
- The intended markets outside Indonesia have not yet been specified.

This is only a preliminary availability screen. PDKI explicitly states that
its data can be incomplete and decisions must not be based on the search
alone. Appropriate professional trademark/name clearance remains required, so
`B002` is not complete.

## Remaining gate decisions

- **B002 — Trademark/name clearance:** obtain and record appropriate clearance
  for Indonesia and every intended launch market.
- **B004 — Android identity:** replace `com.yogeshpaliyal.keypass` only after an
  owner-controlled, globally unique reverse-domain namespace is selected. The
  Kotlin namespace may remain unchanged initially to reduce migration risk.
- **B005 — Signing/store identity:** establish the release keystore owner,
  protected credential-handling process, Play Console account, support contact,
  and final listing identity. Never commit signing secrets or keystores.
- **B006 — Attribution:** `NOTICE.md` now preserves primary KeyPass and Kotpass
  MIT notices, but a complete transitive-dependency notice and final in-app/store
  presentation still require review.

Public/store release preparation remains blocked until those items are closed.

## Implementation verification

- `:app:testFreeDebugUnitTest`: passed.
- `:app:assembleFreeDebug`: passed.
- `:app:assembleFreeRelease`: passed and produced an unsigned APK labelled
  `RAHSA`; signing remains deliberately unresolved under B005.
- Samsung Galaxy A11: updated staging APK installed and launched successfully;
  no fatal application error was observed after launch.
- Release lint still reports the existing `ExtraTranslation` backlog caused by
  locale resources retained after out-of-scope features were removed. The build
  currently does not fail on lint (`abortOnError = false`); release readiness
  must address or explicitly disposition this backlog.
