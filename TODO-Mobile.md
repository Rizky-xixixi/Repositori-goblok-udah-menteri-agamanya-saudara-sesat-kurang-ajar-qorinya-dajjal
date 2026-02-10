# TODO Cleanup Mobile App - COMPLETED ✅

## 🧹 Code Cleanup (Remove Dummy Data)
- [x] Remove `DataSource.kt` (orphan placeholder).
- [x] Clean `model/Guru1.kt` (remove `getDataKehadiran` dummy).
- [x] Clean `model/Siswa1.kt` (remove `getDataKehadiran` dummy).
- [x] Remove legacy `RiwayatKehadiranSiswa.kt` if redundant with `RiwayatKehadiranSiswa1.kt`.
- [x] Remove legacy `RiwayatKehadiranGuruActivity.kt` - Kept both as they serve different roles (Self-view vs Waka-view).

## ⚙️ Dynamic Data Integration (Fix Hardcoded Lists)
- [x] Update `StatistikKehadiran.kt` to fetch real stats from API.
- [x] Update `TotalKelas.kt` to fetch Jurusan from API; Tingkatan/Rombel maintained as static school structures.
- [x] Fix `JadwalPembelajaranGuru.kt` hardcoded class list.
- [x] Verify `LoginAwal.kt` roles are compatible with backend.

## 🧪 Verification
- [x] Build and verify all activities lead to dynamic data.
- [x] Final check on `Evaluasi-Fitur.md` status.
