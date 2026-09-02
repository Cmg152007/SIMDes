package com.example.data.model

data class UserProfile(
    val namaDesa: String = "Desa Cimanggu",
    val kecamatan: String = "Puspahiang",
    val kabupaten: String = "Tasikmalaya",
    val provinsi: String = "Jawa Barat",
    val kodePos: String = "46471",
    val emailDesa: String = "desacimanggu07@gmail.com",
    val namaPetugas: String = "PENDI, S.Sos., M.Si",
    val nipPetugas: String = "19880409 06152007 0002",
    val jabatan: String = "Kasi Pemerintahan",
    val noHp: String = "0812-3456-7890",
    val namaKades: String = "MAIL",
    val nipKades: String = "-",
    val wilayahKerja: String = "", // Cibubuay, Sundawenang, Cimanggu, Mekarlaksana, Mekarjaya, or ""
    val alamatKantor: String = "Jl. Raya Puspahiang - Cimanggu",
    val appsScriptUrl: String = "",
    val githubRepo: String = "rikkinurzaman/data-penduduk",
    val pinSecurity: String = "3522",
    val isBiometricEnabled: Boolean = true,
    val totalRw: Int = 5,
    val totalRt: Int = 22,
    val fotoProfilPath: String = ""
) {
    companion object {
        val WILAYAH_KERJA_OPTIONS = listOf(
            "Semua Wilayah",
            "Cibubuay",
            "Sundawenang",
            "Cimanggu",
            "Mekarlaksana",
            "Mekarjaya"
        )

        fun getRwNumberForWilayah(wilayah: String): Int? {
            return when (wilayah.trim().lowercase()) {
                "cibubuay" -> 1      // RW 001
                "sundawenang" -> 2   // RW 002
                "cimanggu" -> 3      // RW 003
                "mekarlaksana" -> 4  // RW 004
                "mekarjaya" -> 5     // RW 005
                else -> null
            }
        }

        fun getRwLabelForWilayah(wilayah: String): String {
            return when (wilayah.trim().lowercase()) {
                "cibubuay" -> "RW 001"
                "sundawenang" -> "RW 002"
                "cimanggu" -> "RW 003"
                "mekarlaksana" -> "RW 004"
                "mekarjaya" -> "RW 005"
                else -> "Semua RW"
            }
        }

        fun getRtListForWilayah(wilayah: String): List<String> {
            return when (wilayah.trim().lowercase()) {
                "cibubuay" -> listOf("001", "002", "003", "004", "005")
                "sundawenang" -> listOf("006", "007", "008", "009")
                "cimanggu" -> listOf("010", "011", "012", "013", "022")
                "mekarlaksana" -> listOf("014", "015", "016", "017")
                "mekarjaya" -> listOf("018", "019", "020", "021")
                else -> (1..22).map { String.format("%03d", it) }
            }
        }

        fun getRtDescriptionForWilayah(wilayah: String): String {
            return when (wilayah.trim().lowercase()) {
                "cibubuay" -> "RT 001 s/d RT 005"
                "sundawenang" -> "RT 006 s/d RT 009"
                "cimanggu" -> "RT 010, RT 011, RT 012, RT 013, RT 022"
                "mekarlaksana" -> "RT 014 s/d RT 017"
                "mekarjaya" -> "RT 018 s/d RT 021"
                else -> "RT 001 s/d RT 022"
            }
        }
    }
}
