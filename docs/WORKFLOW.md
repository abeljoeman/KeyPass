# Workflow Implementasi RAHSA

Dokumen ini adalah prosedur operasional lintas sesi untuk RAHSA. `ENGINEERING_PRINCIPLES.md` tetap menjadi governance tertinggi; status kerja saat ini ada di `docs/GOVERNANCE_STATUS.md`; standar execution package ada di `docs/IMPLEMENTATION_KIT.md`.

## 1. Pembagian peran

### Planning di ChatGPT

Sebelum implementasi fitur baru:

1. diskusikan requirement dan tujuan produk;
2. audit implementasi/reuse yang sudah ada;
3. review security/privacy dan trade-off;
4. update `PRD.md`;
5. update `TSD.md`, ADR, dan `docs/THREAT_MODEL.md` bila keputusan teknis/security berubah;
6. susun seluruh task phase di `TASKS.md` sebagai task kecil dengan acceptance criteria;
7. owner melakukan approval atas dokumen dan task definition.

Planning dapat menyusun beberapa task untuk satu phase, tetapi tidak ada task yang boleh dieksekusi hanya karena sudah tertulis.

### Implementation preparation di ChatGPT

Setelah task definition disetujui, siapkan **Implementation Kit secara just-in-time untuk task berikutnya saja** terhadap checkpoint terbaru.

Kit wajib memiliki manifest `implementation-kits/Txxx/README.md`. Patch `.patch`, helper `apply-Txxx.ps1`, dan `verify-Txxx.ps1` bersifat opsional.

ChatGPT boleh menyiapkan patch/script sebagai execution aid, tetapi tidak menerapkannya ke application source sebagai bagian dari planning/preparation. Patch/script bukan authority dan tidak menggantikan Codex review/build/test.

Setelah kit siap, aktifkan tepat satu task di `TASKS.md`:

```text
EXECUTION_STATUS: ACTIVE_IMPLEMENTATION
ACTIVE_TASK: Txxx
ACTIVE_KIT: implementation-kits/Txxx/README.md
```

### Execution di Codex

Setelah ada satu active approved Txxx + active kit, Codex:

1. membaca governance/status dan kit;
2. memverifikasi repo/branch/status;
3. menjalankan governance preflight;
4. memvalidasi kit terhadap current source;
5. mengerjakan hanya task tersebut;
6. menjalankan build/test/validasi;
7. membuat focused checkpoint;
8. mengembalikan execution gate ke planning freeze;
9. berhenti setelah task tersebut selesai.

Codex tidak mengambil keputusan product scope atau architecture yang belum disetujui.

## 2. Lifecycle standar satu phase

```text
PLANNING_FREEZE
→ discussion / research
→ PRD / TSD / ADR / Threat Model
→ phase TASKS drafted
→ owner approval
→ prepare JIT kit for next Txxx
→ activate Txxx + ACTIVE_KIT
→ Codex executes one task
→ build / test / checkpoint
→ gate returns to PLANNING_FREEZE
→ prepare next task kit against new checkpoint
→ repeat
```

Jangan menyiapkan full code patches untuk seluruh phase secara default. Patch task berikutnya mudah stale setelah task sebelumnya mengubah source.

## 3. Naming

- Gunakan **RAHSA** untuk produk, requirement, roadmap, task, dan dokumentasi baru.
- Gunakan **KeyPass** hanya untuk upstream open-source project, historical reference, atau legacy package/class/path yang memang masih bernama `keypass`.

## 4. Reuse-first sebelum coding

Untuk setiap capability baru, periksa:

1. retained RAHSA code;
2. upstream KeyPass;
3. Android/AndroidX/Jetpack;
4. mature maintained license-compatible OSS;
5. minimum local implementation.

Jika menambah dependency, dokumentasikan purpose, source, license, alasan reuse/platform tidak cukup, dan security/privacy impact.

Reuse findings yang sudah diverifikasi sebaiknya dicatat di Implementation Kit agar Codex tidak mengulang broad exploration. Security-critical assumption tetap harus diverifikasi ulang bila relevan.

## 5. Menyusun task untuk Codex

Task baru hanya dibuat setelah upstream docs disetujui.

Setiap Txxx harus memiliki:

- satu tujuan sempit;
- referensi PRD/TSD/ADR/Threat Model yang relevan;
- file/scope yang diharapkan bila dapat diketahui;
- acceptance criteria;
- build/test command atau validation expectation;
- security notes bila relevan;
- explicit out-of-scope agar Codex tidak memperluas task;
- reuse/dependency expectation;
- Implementation Kit policy/reference ketika task akan diaktifkan.

Satu task idealnya satu focused commit.

## 6. Menyiapkan Implementation Kit

Ikuti `docs/IMPLEMENTATION_KIT.md` dan gunakan `docs/IMPLEMENTATION_KIT_TEMPLATE.md`.

Kit dibuat terhadap latest verified checkpoint dan hanya untuk next Txxx.

Minimum kit:

```text
implementation-kits/Txxx/README.md
```

Opsional:

```text
implementation-kits/Txxx/Txxx.patch
implementation-kits/Txxx/apply-Txxx.ps1
implementation-kits/Txxx/verify-Txxx.ps1
```

Gunakan patch/script hanya bila benar-benar mengurangi exploration atau pekerjaan mekanis. Jangan membuat helper kompleks untuk task yang lebih sederhana dikerjakan langsung oleh Codex.

Kit harus menyebut risk class dan model/reasoning recommendation. Pilih model berdasarkan inherent task risk terlebih dahulu; kit completeness hanya dapat mengurangi exploration cost, bukan security-risk floor.

## 7. Handoff ke Codex

Handoff minimum:

```text
Repo: G:\Projects\KeyPass
Product: RAHSA
Branch: <branch>
Baseline: v0.2-prototype
Active task: Txxx
Active kit: implementation-kits/Txxx/README.md
Checkpoint terakhir: <commit>
Instruksi: ikuti governance repo, jalankan hanya active Txxx, gunakan kit sebagai non-authoritative execution aid, validasi/build/test, checkpoint, reset gate, lalu stop.
```

Tidak perlu menyalin seluruh governance atau seluruh implementation plan ke prompt Codex; source of truth dan kit harus hidup di repository.

## 8. Siklus satu task Codex

1. `git status -sb` dan verifikasi HEAD/branch.
2. Baca exact task, source-of-truth documents, dan active kit.
3. Jalankan:

```powershell
python scripts/governance_preflight.py --implementation Txxx
```

4. Inspeksi file target dan validasi reuse/assumption dari kit.
5. Jika ada patch, jalankan `git apply --check` ketika praktis; jangan force patch stale.
6. Terapkan perubahan paling kecil yang memenuhi requirement.
7. Review `git diff --check` dan full `git diff`; jangan biarkan unrelated changes.
8. Jalankan targeted unit/instrumentation tests sesuai task.
9. Jalankan build relevan; untuk Android saat ini umumnya `./gradlew :app:assembleFreeDebug` bila task membutuhkan build aplikasi.
10. Jalankan smoke/device validation bila acceptance criteria memerlukannya.
11. Ubah hanya task aktif menjadi complete setelah seluruh validation lulus.
12. Set execution gate menjadi:

```text
EXECUTION_STATUS: PLANNING_FREEZE
ACTIVE_TASK: NONE
ACTIVE_KIT: NONE
```

13. Stage hanya task-owned files, kit/task-status completion changes yang relevan, dan jangan memakai `git add .`.
14. Periksa staged diff/check.
15. Commit focused checkpoint.
16. Report checkpoint dan stop.

Jika implementation membuka keputusan product/security/architecture baru, **jangan putuskan lokal**. Stop dan kembalikan ke planning di ChatGPT.

## 9. Patch dan PowerShell helper

Patch/helper bersifat opsional.

Aturan utama:

- patch harus narrow dan prepared-against checkpoint harus tercatat;
- jangan force conflict;
- script harus deterministic, fail-fast, secret-safe, dan explicit-path;
- script tidak boleh melakukan broad staging, commit, push, menyimpan secret, atau bypass validation;
- hasil perubahan harus selalu bisa direview melalui git diff;
- jika kit stale secara material, Codex harus meminta refresh atau mengadaptasi hanya perubahan trivial yang tetap berada di scope dan tidak menimbulkan keputusan baru.

## 10. Checkpoint task

Gunakan format:

```text
Checkpoint Txxx selesai
- Commit/HEAD: <hash> <subject>
- Branch: <branch>
- Working tree: <clean/intentional changes>
- Build/test: <commands + result>
- Smoke/device test: <result>
- Implementation Kit: <path + used/partially used/not used>
- Reuse/dependency note: <if relevant>
- Blocker/decision needed: <if any>
- Execution gate: PLANNING_FREEZE / NONE / NONE
- Berikutnya: stop; return to ChatGPT/owner before activating another task
```

Codex tidak otomatis memulai task berikutnya.

## 11. Cost-effective development

Model/reasoning dipilih per task:

- deterministic low-risk kit/mechanical/smoke → Luna + low;
- low-risk task dengan limited adaptation → Luna + low/medium;
- normal focused implementation/review → Terra + medium;
- security-sensitive vault/master-password/authentication/Keystore/recovery semantics, hard races, architecture-critical changes → pertimbangkan Sol dengan alasan konkret.

Kit dapat menghemat exploration/coding token, tetapi tidak boleh menurunkan independent verification pada security boundary.

Selalu gunakan lowest sufficient cost. Security/correctness tetap lebih penting daripada penghematan token.

## 12. Current workstream

Saat ini:

- `v0.2-prototype` adalah stable baseline RAHSA;
- tidak ada active implementation task;
- `ACTIVE_KIT: NONE`;
- Phase 13 masih tahap product/security planning;
- public/Play Store release work PARKED;
- B002/B004/B005/B006 bukan task aktif.

Jangan menjalankan feature implementation sampai `TASKS.md` berubah dari planning freeze menjadi satu approved active Txxx dengan active Implementation Kit.
