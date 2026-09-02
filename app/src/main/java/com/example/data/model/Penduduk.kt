package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "penduduk")
data class Penduduk(
    @PrimaryKey
    val nik: String, // 3. NIK (Primary Identifier)
    val no: Int = 1, // 1. NO
    val nama: String = "", // 2. NAMA
    val jenisKelamin: String = "LAKI-LAKI", // 4. JENIS KELAMIN (LAKI-LAKI / PEREMPUAN)
    val tempatLahir: String = "", // 5. TEMPAT LAHIR
    val tanggalLahir: String = "", // 6. TANGGAL LAHIR (YYYY-MM-DD)
    val agama: String = "ISLAM", // 7. AGAMA
    val pendidikanTerakhir: String = "SLTA / SEDERAJAT", // 8. PENDIDIKAN TERAKHIR
    val pekerjaan: String = "WIRASWASTA", // 9. PEKERJAAN
    val gdr: String = "TIDAK TAHU", // 10. GDR (Golongan Darah: A, B, AB, O, TIDAK TAHU)
    val statusPerkawinan: String = "BELUM KAWIN", // 11. STATUS PERKAWINAN
    val bukuNikah: String = "TIDAK ADA", // 12. BUKU NIKAH (ADA / TIDAK ADA)
    val shdk: String = "KEPALA KELUARGA", // 13. SHDK (Status Hubungan Dalam Keluarga)
    val kewarganegaraan: String = "WNI", // 14. KEWARGANEGARAAN
    val noPaspor: String = "-", // 15. NO. PASPOR
    val noKitas: String = "-", // 16. NO KITAS
    val namaAyah: String = "", // 17. NAMA AYAH
    val namaIbu: String = "", // 18. NAMA IBU
    val noKk: String = "", // 19. NO KK
    val namaKepalaKeluarga: String = "", // 20. NAMA KK
    val alamat: String = "Dusun Cimanggu", // 21. ALAMAT
    val rw: String = "001", // 22. RW (Format 3 digit angka, misal: 001)
    val rt: String = "001", // 23. RT (Format 3 digit angka, misal: 001)
    val umur: Int = 0, // 24. UMUR
    val umurLakiLaki: String = "", // Legacy field (optional)
    val umurPerempuan: String = "", // Legacy field (optional)
    val kepemilikanEKtp: String = "SUDAH MEMILIKI", // 25. KEPEMILIKAN E-KTP
    val tanggalPencetakan: String = "", // 26. TANGGAL PENCETAKAN
    val kepemilikanAktaKelahiran: String = "ADA", // 27. KEPEMILIKAN AKTA KELAHIRAN
    val kartuKia: String = "ADA", // 28. KARTU KIA
    val kartuPkh: String = "TIDAK", // 29. KARTU PKH (YA / TIDAK)
    val kartuBpnt: String = "TIDAK", // 30. KARTU BPNT (YA / TIDAK)
    val kartuBpjsKis: String = "BPJS PBI / KIS", // 31. KARTU BPJS/KIS
    val kartuKip: String = "TIDAK", // 32. KARTU KIP (YA / TIDAK)
    val jenisKb: String = "BUKAN PESERTA KB", // 33. JENIS KB
    val usahaYangDijalankan: String = "-", // 34. USAHA YANG DIJALANKAN
    val listrikJenis: String = "TOKEN", // 35. LISTRIK (TOKEN/ PASCA BAYAR)
    val kepemilikanListrik: String = "SENDIRI", // 36. KEPEMILIKAN LISTRIK
    val dayaListrik: String = "900 VA", // 37. DAYA LISTRIK
    val noTokenKwh: String = "-", // 38. NO TOKEN / KWH
    val noHandphone: String = "-", // 39. NO HANDPHONE
    val anakKe: Int = 1, // 40. ANAK KE
    val kepemilikanRumah: String = "MILIK SENDIRI", // 41. KEPEMILIKAN RUMAH
    val ukuranRumah: String = "6x8 m", // 42. UKURAN RUMAH
    val jenisRumah: String = "PERMANEN", // 43. JENIS RUMAH
    val keterangan: String = "Aktif", // 44. KETERANGAN
    val vaksinasi: String = "DOSIS LENGKAP", // 45. VAKSINASI
    val disabilitas: String = "TIDAK ADA", // 46. DISABILITAS
    val statusMutasi: String = "AKTIF", // Status Mutasi: AKTIF, MENINGGAL, PINDAH
    // Field Data Kematian
    val tanggalKematian: String = "",
    val waktuKematian: String = "",
    val tempatKematian: String = "",
    val penyebabKematian: String = "",
    val tempatPemakaman: String = "",
    val noSuratKematian: String = "",
    val namaPelaporKematian: String = "",
    val hubunganPelaporKematian: String = "",
    val catatanKematian: String = "",
    // Field Data Kepindahan (Pindah Keluar)
    val tanggalPindah: String = "",
    val alasanPindah: String = "",
    val klasifikasiPindah: String = "",
    val alamatTujuan: String = "",
    val rtTujuan: String = "",
    val rwTujuan: String = "",
    val desaTujuan: String = "",
    val kecamatanTujuan: String = "",
    val kabupatenTujuan: String = "",
    val provinsiTujuan: String = "",
    val kodePosTujuan: String = "",
    val noSuratPindah: String = "",
    val catatanPindah: String = "",
    val syncedWithSheets: Boolean = false,
    val lastModifiedTimestamp: Long = System.currentTimeMillis()
) {
    fun isMeninggal(): Boolean = statusMutasi.equals("MENINGGAL", ignoreCase = true)
    fun isPindah(): Boolean = statusMutasi.equals("PINDAH", ignoreCase = true)
    fun isAktif(): Boolean = !isMeninggal() && !isPindah()
    fun isMale(): Boolean {
        val clean = jenisKelamin.trim().uppercase()
        return clean == "LAKI-LAKI" || clean == "LAKI - LAKI" || clean == "LAKI LAKI" || clean == "L" || clean == "LK" || clean.startsWith("LAKI")
    }

    fun isFemale(): Boolean {
        val clean = jenisKelamin.trim().uppercase()
        return clean == "PEREMPUAN" || clean == "P" || clean == "PR" || clean == "WANITA" || clean.startsWith("PEREM") || clean.startsWith("WANI")
    }

    fun getFormattedGender(): String {
        return if (isMale()) "LAKI-LAKI" else if (isFemale()) "PEREMPUAN" else jenisKelamin.ifBlank { "LAKI-LAKI" }
    }

    fun getGenderDisplayLabel(): String {
        return if (isMale()) "Laki-laki" else if (isFemale()) "Perempuan" else jenisKelamin.ifBlank { "Laki-laki" }
    }

    /**
     * Menghitung usia otomatis secara dinamis dari tanggal lahir terhadap tahun berjalan.
     * Jika tanggal lahir kosong, menggunakan nilai umur tersimpan.
     */
    fun getEffectiveAge(): Int {
        val calculated = calculateAge(tanggalLahir)
        return if (calculated > 0) calculated else if (umur > 0) umur else 0
    }

    fun getUmurLakiLakiDisplay(): String {
        val age = getEffectiveAge()
        return if (isMale()) "$age Tahun" else "-"
    }

    fun getUmurPerempuanDisplay(): String {
        val age = getEffectiveAge()
        return if (isFemale()) "$age Tahun" else "-"
    }

    fun isPenerimaBansos(): Boolean = kartuPkh.equals("YA", ignoreCase = true) || 
            kartuBpnt.equals("YA", ignoreCase = true) || 
            kartuKip.equals("YA", ignoreCase = true) || 
            kartuBpjsKis.contains("PBI", ignoreCase = true)

    fun isBalita(): Boolean = getEffectiveAge() in 0..5
    fun isLansia(): Boolean = getEffectiveAge() >= 60
    fun isWajibKtp(): Boolean = getEffectiveAge() >= 17
    fun hasDisabilitas(): Boolean = !disabilitas.equals("TIDAK ADA", ignoreCase = true) && disabilitas.isNotBlank()

    // Helper to calculate or format age safely
    companion object {
        fun getCurrentRunningYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

        fun normalizeGender(input: String): String {
            val clean = input.trim().uppercase()
            return when {
                clean == "LAKI-LAKI" || clean == "LAKI - LAKI" || clean == "LAKI LAKI" || clean == "L" || clean == "LK" || clean.startsWith("LAKI") -> "LAKI-LAKI"
                clean == "PEREMPUAN" || clean == "P" || clean == "PR" || clean == "WANITA" || clean.startsWith("PEREM") || clean.startsWith("WANI") -> "PEREMPUAN"
                else -> if (clean.isNotBlank()) clean else "LAKI-LAKI"
            }
        }

        /**
         * Menghitung usia secara otomatis dari string tanggal lahir terhadap tahun berjalan saat ini.
         * Mendukung format YYYY-MM-DD, DD-MM-YYYY, YYYY/MM/DD, DD/MM/YYYY, dsb.
         */
        fun calculateAge(tanggalLahir: String): Int {
            if (tanggalLahir.isBlank()) return 0
            return try {
                val clean = tanggalLahir.trim().take(10)
                val parts = when {
                    clean.contains("-") -> clean.split("-")
                    clean.contains("/") -> clean.split("/")
                    clean.contains(".") -> clean.split(".")
                    clean.contains(" ") -> clean.split(" ")
                    else -> emptyList()
                }
                val now = Calendar.getInstance()
                val currentYear = now.get(Calendar.YEAR)
                val currentMonth = now.get(Calendar.MONTH) + 1
                val currentDay = now.get(Calendar.DAY_OF_MONTH)

                if (parts.size == 3) {
                    val p0 = parts[0].trim().toIntOrNull() ?: 0
                    val p1 = parts[1].trim().toIntOrNull() ?: 0
                    val p2 = parts[2].trim().toIntOrNull() ?: 0

                    val birthYear: Int
                    val birthMonth: Int
                    val birthDay: Int

                    if (p0 > 1000) {
                        // Format: YYYY-MM-DD
                        birthYear = p0
                        birthMonth = p1.coerceIn(1, 12)
                        birthDay = p2.coerceIn(1, 31)
                    } else if (p2 > 1000) {
                        // Format: DD-MM-YYYY
                        birthYear = p2
                        birthMonth = p1.coerceIn(1, 12)
                        birthDay = p0.coerceIn(1, 31)
                    } else {
                        return 0
                    }

                    if (birthYear <= 1900 || birthYear > currentYear) return 0

                    var age = currentYear - birthYear
                    if (currentMonth < birthMonth || (currentMonth == birthMonth && currentDay < birthDay)) {
                        age--
                    }
                    maxOf(0, age)
                } else if (clean.length == 4 && clean.toIntOrNull() != null) {
                    val birthYear = clean.toInt()
                    if (birthYear in 1900..currentYear) {
                        maxOf(0, currentYear - birthYear)
                    } else 0
                } else 0
            } catch (e: Exception) {
                0
            }
        }

        /**
         * Memastikan format RT dan RW selalu berupa 3 digit karakter angka (contoh: "001", "002", "012")
         */
        fun formatRtRw(input: String): String {
            val digits = input.filter { it.isDigit() }
            return if (digits.isBlank()) "001" else digits.padStart(3, '0').takeLast(3)
        }

        val AGAMA_OPTIONS = listOf("ISLAM", "KRISTEN", "KATOLIK", "HINDU", "BUDDHA", "KONGHUCU")
        val GENDER_OPTIONS = listOf("LAKI-LAKI", "PEREMPUAN")
        val GDR_OPTIONS = listOf("A", "B", "AB", "O", "TIDAK TAHU")
        val STATUS_PERKAWINAN_OPTIONS = listOf("BELUM KAWIN", "KAWIN", "CERAI HIDUP", "CERAI MATI")
        val BUKU_NIKAH_OPTIONS = listOf("ADA", "TIDAK ADA")
        val SHDK_OPTIONS = listOf("KEPALA KELUARGA", "SUAMI", "ISTRI", "ANAK", "MENANTU", "CUCU", "ORANG TUA", "MERTUA", "FAMILI LAIN", "PEMBANTU", "LAINNYA")
        val KEWARGANEGARAAN_OPTIONS = listOf("WNI", "WNA")
        val EKTP_OPTIONS = listOf("SUDAH MEMILIKI", "BELUM MEMILIKI", "BELUM WAJIB KTP")
        val AKTA_OPTIONS = listOf("ADA", "TIDAK ADA")
        val KIA_OPTIONS = listOf("ADA", "TIDAK ADA", "TIDAK WAJIB")
        val YES_NO_OPTIONS = listOf("YA", "TIDAK")
        val BPJS_OPTIONS = listOf("BPJS PBI / KIS", "BPJS MANDIRI", "BPJS KETENAGAKERJAAN", "TIDAK MEMILIKI")
        val KB_OPTIONS = listOf("IUD", "MOW", "MOP", "KONDOM", "IMPLAN", "SUNTIK", "PIL", "BUKAN PESERTA KB")
        val LISTRIK_JENIS_OPTIONS = listOf("TOKEN", "PASCA BAYAR", "MENUMPANG / NON-METERAN")
        val KEPEMILIKAN_LISTRIK_OPTIONS = listOf("SENDIRI", "MENUMPANG", "BERSAMA")
        val DAYA_LISTRIK_OPTIONS = listOf("450 VA", "900 VA", "1300 VA", "2200 VA", "3500 VA+", "TIDAK ADA")
        val KEPEMILIKAN_RUMAH_OPTIONS = listOf("MILIK SENDIRI", "SEWA / KONTRAK", "MENUMPANG / KELUARGA", "DINAS")
        val JENIS_RUMAH_OPTIONS = listOf("PERMANEN", "SEMI PERMANEN", "NON PERMANEN / PANGGUNG")
        val VAKSIN_OPTIONS = listOf("DOSIS 1", "DOSIS 2", "BOOSTER 1", "BOOSTER 2", "DOSIS LENGKAP", "BELUM VAKSIN")
        val DISABILITAS_OPTIONS = listOf("TIDAK ADA", "TUNA NETRA", "TUNA RUNGU", "TUNA WICARA", "TUNA DAKSA", "TUNA GRAHITA", "GANDA", "LAINNYA")
        val PENDIDIKAN_OPTIONS = listOf(
            "TIDAK / BELUM SEKOLAH",
            "SD / SEDERAJAT",
            "SLTP / SEDERAJAT",
            "SLTA / SEDERAJAT",
            "DIPLOMA I / II",
            "AKADEMI / DIPLOMA III / S. MUDA",
            "DIPLOMA IV / STRATA I",
            "STRATA II",
            "STRATA III"
        )
        val STATUS_MUTASI_OPTIONS = listOf("AKTIF", "MENINGGAL", "PINDAH")
        val TEMPAT_KEMATIAN_OPTIONS = listOf("Rumah Tinggal", "Rumah Sakit", "Puskesmas", "Klinik", "Tempat Kerja", "Perjalanan", "Lainnya")
        val PENYEBAB_KEMATIAN_OPTIONS = listOf("Sakit Biasa / Medis", "Sakit Menular", "Sakit Menahun / Usia Lanjut", "Kecelakaan Lalu Lintas", "Kecelakaan Kerja", "Lainnya")
        val ALASAN_PINDAH_OPTIONS = listOf("Pekerjaan / Dinas", "Pendidikan / Sekolah", "Pernikahan / Mengikuti Suami/Istri", "Keluarga / Mengikuti Orang Tua", "Perumahan / Domisili Baru", "Kesehatan", "Lainnya")
        val KLASIFIKASI_PINDAH_OPTIONS = listOf("Anggota Keluarga Saja", "Kepala Keluarga Saja", "Kepala Keluarga & Seluruh Anggota", "Kepala Keluarga & Sebagian Anggota")
    }
}
