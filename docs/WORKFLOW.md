# Workflow Implementasi RAHSA

Dokumen ini adalah prosedur operasional lintas sesi untuk RAHSA. `ENGINEERING_PRINCIPLES.md` tetap menjadi governance tertinggi; status kerja saat ini ada di `docs/GOVERNANCE_STATUS.md`.

## 1. Pembagian peran

### Planning di ChatGPT

Sebelum implementasi fitur baru, lakukan:

1. diskusi requirement dan tujuan produk;
2. audit implementasi/reuse yang sudah ada;
3. review security/privacy dan trade-off;
4. update `PRD.md`;
5. update `TSD.md`, ADR, dan `docs/THREAT_MODEL.md` bila keputusan teknis/security berubah;
6. susun `TASKS.md` menjadi task kecil dengan acceptance criteria;
7. owner melakukan approval.

Tidak ada handoff implementasi sebelum dokumen yang diperlukan disetujui.

### Execution di Codex

Setelah ada satu active approved Txxx, Codex:

1. membaca governance/status;
2. memverifikasi repo/branch/status;
3. menjalankan governance preflight;
4. mengerjakan hanya task tersebut;
5. menjalankan build/test/validasi;
6. membuat focused checkpoint;
7. berhenti setelah task tersebut selesai.

Codex tidak mengambil keputusan product scope atau architecture yang belum disetujui.

## 2. Naming

- Gunakan **RAHSA** untuk produk, requirement, roadmap, task, dan dokumentasi baru.
- Gunakan **KeyPass** hanya untuk upstream open-source project, historical reference, atau legacy package/class/path yang memang masih bernama `keypass`.

## 3. Reuse-first sebelum coding

Untuk setiap capability baru, periksa:

1. retained RAHSA code;
2. upstream KeyPass;
3. Android/AndroidX/Jetpack;
4. mature maintained license-compatible OSS;
5. minimum local implementation.

Jika menambah dependency, dokumentasikan purpose, source, license, alasan reuse/platform tidak cukup, dan security/privacy impact.

## 4. Menyusun task untuk Codex

Task baru hanya dibuat setelah upstream docs disetujui.

Setiap Txxx harus memiliki:

- satu tujuan sempit;
- referensi PRD/TSD/ADR/Threat Model yang relevan;
- file/scope yang diharapkan bila dapat diketahui;
- acceptance criteria;
- build/test command atau validation expectation;
- security notes bila relevan;
- explicit out-of-scope agar Codex tidak memperluas task.

Satu task idealnya satu focused commit.

## 5. Handoff ke Codex

Handoff minimum:

```text
Repo: G:\Projects\KeyPass
Product: RAHSA
Branch: <branch>
Baseline: v0.2-prototype
Active task: <exact Txxx line>
Checkpoint terakhir: <commit>
Instruksi: ikuti AGENTS.md, docs/GOVERNANCE_STATUS.md, ENGINEERING_PRINCIPLES.md, docs/WORKFLOW.md, dan source-of-truth task. Jalankan hanya task aktif dan berhenti setelah checkpoint.
```

Tidak perlu menyalin seluruh governance ke prompt Codex; governance harus hidup di repository.

## 6. Siklus satu task Codex

1. `git status -sb` dan verifikasi HEAD/branch.
2. Baca exact task dan dokumen sumber kebenaran.
3. Jalankan:

```powershell
python scripts/governance_preflight.py --implementation Txxx
```

4. Inspeksi reuse options dan current implementation.
5. Terapkan perubahan paling kecil yang memenuhi requirement.
6. Review `git diff`; jangan biarkan unrelated changes.
7. Jalankan targeted unit/instrumentation tests sesuai task.
8. Jalankan build relevan; untuk prototype Android saat ini umumnya `./gradlew :app:assembleFreeDebug` bila task membutuhkan build aplikasi.
9. Jalankan smoke/device validation bila acceptance criteria memerlukannya.
10. Ubah hanya checkbox/status task aktif setelah seluruh validation lulus.
11. Stage hanya task-owned files.
12. Periksa staged diff/check.
13. Commit focused checkpoint.
14. Report checkpoint dan stop.

Jika implementation membuka keputusan product/security/architecture baru, **jangan putuskan lokal**. Stop dan kembalikan ke planning di ChatGPT.

## 7. Checkpoint task

Gunakan format:

```text
Checkpoint Txxx selesai
- Commit/HEAD: <hash> <subject>
- Branch: <branch>
- Working tree: <clean/intentional changes>
- Build/test: <commands + result>
- Smoke/device test: <result>
- Reuse/dependency note: <if relevant>
- Blocker/decision needed: <if any>
- Berikutnya: stop; owner decides next active task
```

Codex tidak otomatis memulai task berikutnya.

## 8. Cost-effective development

Model/reasoning dipilih per task, bukan per project:

- inspection/mechanical/smoke → Luna + low;
- normal focused implementation → Terra + medium;
- security-sensitive vault/master-password/authentication semantics, hard races, architecture-critical changes → pertimbangkan Sol dengan alasan konkret.

Selalu gunakan lowest sufficient cost. Security/correctness tetap lebih penting daripada penghematan token.

## 9. Current workstream

Saat ini:

- `v0.2-prototype` adalah stable baseline RAHSA;
- tidak ada active implementation task;
- Phase 13 masih tahap product/security planning;
- public/Play Store release work PARKED;
- B002/B004/B005/B006 bukan task aktif.

Jangan menjalankan feature implementation sampai `TASKS.md` berubah dari planning freeze menjadi satu approved active Txxx.
