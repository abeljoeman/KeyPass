# Workflow Implementasi KeyPass

Dokumen ini adalah panduan operasional untuk melanjutkan pekerjaan KeyPass lintas sesi. Gunakan bersama `TASKS.md`, `PRD.md`, `TSD.md`, `ENGINEERING_PRINCIPLES.md`, dan dokumen relevan di `docs/`. Jika ada konflik, dokumen sumber tersebut tetap menjadi acuan utama.

## 1. Prinsip kerja

- Kerjakan task sesuai urutan di `TASKS.md`, kecuali task ditandai dapat berjalan paralel.
- Ambil hanya satu task kecil pada satu waktu. Jangan memperluas scope atau melakukan refactor yang tidak diperlukan.
- Sebelum mengubah kode, baca definisi task secara exact beserta acceptance/checkpoint dan file sumber kebenaran yang dirujuk.
- Pertahankan perilaku yang sudah benar. Setiap perubahan harus mudah ditinjau, diuji, dan dibatalkan.
- Jangan menebak scope task berikutnya. Baca langsung bagian terkait dari `TASKS.md`.

## 2. Cara membuat perubahan

- Utamakan patch terarah untuk perubahan kecil dan terkontrol.
- Untuk perubahan mekanis berulang, gunakan script sekali pakai yang deterministik, lalu periksa diff secara penuh.
- Jangan menulis ulang seluruh file jika perubahan lokal sudah cukup.
- Jangan mengubah file di luar scope task aktif.
- Di PowerShell, gunakan `-LiteralPath` untuk path yang sudah diketahui dan hindari wildcard yang tidak perlu.
- Waspadai encoding dan line ending. Pertahankan encoding file asal; jangan memakai `Set-Content` atau `Out-File` tanpa encoding eksplisit karena perilakunya berbeda antarversi PowerShell dan dapat mengubah UTF-8/BOM atau CRLF/LF. Setelah script menyentuh file teks, periksa diff untuk karakter rusak dan perubahan seluruh file yang tidak disengaja.

## 3. Siklus satu task

1. Pastikan branch dan working tree diketahui dengan `git status -sb`.
2. Baca task aktif secara exact dari `TASKS.md` dan identifikasi scope serta acceptance-nya.
3. Inspeksi implementasi saat ini dan dokumen sumber kebenaran yang relevan.
4. Terapkan perubahan paling kecil yang memenuhi task.
5. Tinjau `git diff` dan pastikan tidak ada perubahan di luar scope.
6. Jalankan build yang relevan sebelum commit. Build harus lulus; jangan commit perubahan yang belum berhasil dibangun.
7. Lakukan smoke test secara manual, satu skenario pada satu waktu. Catat hasil tiap skenario sebelum beralih ke skenario berikutnya agar penyebab kegagalan tetap jelas.
8. Setelah build dan smoke test lulus, ubah checkbox task di `TASKS.md` dari `[ ]` menjadi `[x]` dengan exact replacement, bukan patch berbasis konteks. Pastikan hanya baris task yang dimaksud berubah.
9. Stage hanya file milik task aktif, lalu periksa staged diff.
10. Commit sebagai satu checkpoint yang fokus.
11. Verifikasi status dan log setelah commit.

## 4. Build dan smoke test Android

- Gunakan Gradle Wrapper milik repo dan variant yang sesuai dengan project. Untuk alur prototype saat ini, build utama adalah `./gradlew :app:assembleFreeDebug` (di PowerShell dapat memakai `.\\gradlew.bat :app:assembleFreeDebug`).
- Build dilakukan sebelum staging final dan commit. Jika build gagal, perbaiki dalam scope task atau hentikan dan laporkan blocker; jangan menandai task selesai.
- Untuk pengujian emulator, targetkan serial ADB secara eksplisit: `emulator-5554`.
- Contoh pola perintah: `adb -s emulator-5554 ...`. Jangan mengandalkan device default ketika ada lebih dari satu target ADB.
- Pastikan device tersedia sebelum instalasi/tes dengan `adb devices`.
- Jalankan smoke test satu per satu: instal/luncurkan aplikasi, lakukan satu alur, amati hasil, lalu lanjut ke alur berikutnya. Jangan menggabungkan banyak skenario menjadi satu verdict yang sulit ditelusuri.
- Jika validasi harus dilakukan pada perangkat fisik sesuai task, hasil emulator tidak menggantikannya.

## 5. Memperbarui `TASKS.md`

- Checklist hanya boleh diubah setelah implementasi, build, dan validasi task selesai.
- Gunakan exact string replacement untuk mengganti baris lengkap `- [ ] **Txxx** ...` menjadi `- [x] **Txxx** ...`.
- Jangan menggunakan patch kontekstual untuk checklist `TASKS.md`; ini mengurangi risiko salah mencentang task yang mirip atau bergeser.
- Dalam script replacement, hentikan proses jika baris lama tidak ditemukan atau baris baru sudah ada. Baca file sebagai satu string, ganti tepat satu kemunculan, lalu tulis kembali dengan encoding UTF-8 yang ditentukan secara eksplisit sambil mempertahankan line ending asal.
- Setelah replacement, tampilkan/periksa baris Txxx tersebut dan `git diff -- TASKS.md`.
- Jangan sekaligus mencentang task berikutnya atau mengubah redaksi task.

## 6. Staging dan commit

- Jangan memakai `git add .` atau staging luas. Stage daftar file exact yang termasuk dalam task.
- Sebelum commit, jalankan:
  - `git status -sb`
  - `git diff --cached --check`
  - `git diff --cached --stat`
  - `git diff --cached`
- Pastikan staged diff hanya memuat implementasi task dan satu perubahan checklist yang sesuai.
- Gunakan pesan commit singkat dan spesifik, mengikuti pola histori repo, misalnya `ui: ...`, `fix: ...`, `test: ...`, atau `docs: ...`.
- Satu task idealnya menghasilkan satu commit checkpoint. Jangan menyertakan perubahan lokal milik user atau task lain.
- Setelah commit, verifikasi dengan `git status -sb` dan `git log -3 --oneline --decorate`. Working tree harus bersih sebelum melanjutkan.

## 7. Checkpoint dan handoff sesi

Pada akhir task, catat checkpoint dengan format berikut:

```text
Checkpoint Txxx selesai
- Commit/HEAD: <hash> <subject>
- Branch: <nama-branch>
- Status terhadap origin: <ahead/behind>
- Working tree: <bersih atau daftar perubahan yang sengaja tersisa>
- Build/test: <perintah dan hasil>
- Smoke test: <skenario yang lulus/gagal>
- Catatan/blocker: <jika ada>
- Berikutnya: <Txxx dan judul exact dari TASKS.md>
```

Saat memulai sesi baru, berikan handoff minimum berikut kepada asisten:

```text
Repo: G:\Projects\KeyPass
Branch: <nama-branch>
Checkpoint terakhir: <Txxx, commit hash, subject>
Status working tree: <hasil git status -sb>
Task berikutnya: <baris exact dari TASKS.md>
Build terakhir: <perintah dan hasil>
Smoke test terakhir: <hasil>
Instruksi: baca docs/WORKFLOW.md dan dokumen sumber kebenaran terkait sebelum mengubah kode.
```

Asisten pada sesi baru harus memverifikasi informasi tersebut langsung dari repo sebelum mulai bekerja.

## 8. Batas sesi per phase

- Selesaikan seluruh task dalam satu phase, termasuk build/test, smoke test, checklist, commit, dan checkpoint akhir phase.
- Setelah phase selesai dan working tree bersih, hentikan pekerjaan pada sesi tersebut.
- Buat handoff akhir phase menggunakan format di atas, sebutkan seluruh task phase yang selesai dan task pertama phase berikutnya secara exact.
- Mulai phase berikutnya di sesi chat baru. Jangan memulai implementasi phase baru di sesi lama.
- Sesi baru harus dimulai dengan membaca `docs/WORKFLOW.md`, memeriksa `git status -sb` dan commit terakhir, lalu membaca scope phase berikutnya langsung dari `TASKS.md`.

## 9. Checkpoint saat ini

- Phase 12 selesai sampai T125.
- Checkpoint Phase 12: `85d39e3 test: record phase 12 physical ux validation`.
- Branch: `prototype/v0.1`; checkpoint tersebut 21 commit di depan `origin/prototype/v0.1`.
- Brand Decision Gate sedang berjalan: nama **RAHSA** dan primary mark sudah dipilih.
- Berikutnya: selesaikan B002, B004, B005, dan B006 sesuai `docs/BRAND_DECISION.md`; public/store release tetap diblokir sampai seluruh gate selesai.
