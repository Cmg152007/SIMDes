package com.example.data.util

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.example.data.model.Penduduk
import com.example.data.model.UserProfile
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Locale

data class DusunConfig(
    val name: String,
    val rwLabel: String,
    val rwNumber: Int,
    val rtList: List<String>,
    val luasKm: Double
)

data class AgeRangeCount(
    val male: Int = 0,
    val female: Int = 0
) {
    val total: Int get() = male + female
}

data class Format1Row(
    val no: Int,
    val namaWilayah: String,
    val age0to5: AgeRangeCount = AgeRangeCount(),
    val age6to12: AgeRangeCount = AgeRangeCount(),
    val age13to15: AgeRangeCount = AgeRangeCount(),
    val age16to18: AgeRangeCount = AgeRangeCount(),
    val age19to24: AgeRangeCount = AgeRangeCount(),
    val age25to29: AgeRangeCount = AgeRangeCount(),
    val age30to34: AgeRangeCount = AgeRangeCount(),
    val age35to39: AgeRangeCount = AgeRangeCount(),
    val age40to44: AgeRangeCount = AgeRangeCount(),
    val age45to49: AgeRangeCount = AgeRangeCount(),
    val age50to54: AgeRangeCount = AgeRangeCount(),
    val age55to59: AgeRangeCount = AgeRangeCount(),
    val age60to64: AgeRangeCount = AgeRangeCount(),
    val age65to69: AgeRangeCount = AgeRangeCount(),
    val age70to74: AgeRangeCount = AgeRangeCount(),
    val age75Above: AgeRangeCount = AgeRangeCount(),
    val totalMale: Int = 0,
    val totalFemale: Int = 0
) {
    val grandTotal: Int get() = totalMale + totalFemale
}

data class Format2PendidikanPekerjaanRow(
    val no: Int,
    val namaWilayah: String,
    // Pendidikan
    val belumSekolah: Int = 0,
    val tidakTamatSd: Int = 0,
    val tamatSd: Int = 0,
    val tamatSmp: Int = 0,
    val tamatSma: Int = 0,
    val diploma12: Int = 0,
    val diploma3: Int = 0,
    val diploma4S1: Int = 0,
    val strata2: Int = 0,
    val strata3: Int = 0,
    val totalPendidikan: Int = 0,
    // Mata Pencaharian
    val pnsTniPolri: Int = 0,
    val karyawan: Int = 0,
    val buruh: Int = 0,
    val petani: Int = 0,
    val peternak: Int = 0,
    val nelayan: Int = 0,
    val wiraswasta: Int = 0,
    val pelajar: Int = 0,
    val belumBekerja: Int = 0,
    val lainnya: Int = 0,
    val totalPekerjaan: Int = 0
)

data class Format2AgamaKewarganegaraanRow(
    val no: Int,
    val namaWilayah: String,
    // Agama
    val islam: Int = 0,
    val kristen: Int = 0,
    val hindu: Int = 0,
    val budha: Int = 0,
    val khonghucu: Int = 0,
    val kepercayaan: Int = 0,
    val totalAgama: Int = 0,
    // Kewarganegaraan
    val wna: Int = 0,
    val wni: Int = 0,
    val totalKewarganegaraan: Int = 0
)

data class GenderCount(
    val male: Int = 0,
    val female: Int = 0
) {
    val total: Int get() = male + female
}

data class Format3Row(
    val no: Int,
    val namaWilayah: String,
    val luasWilayahKm: Double,
    val jumlahRt: Int,
    val jumlahRw: Int,
    val jumlahDusun: Int,
    // Mutasi
    val blnLalu: GenderCount = GenderCount(),
    val lahirBlnIni: GenderCount = GenderCount(),
    val matiBlnIni: GenderCount = GenderCount(),
    val datangBlnIni: GenderCount = GenderCount(),
    val pindahBlnIni: GenderCount = GenderCount(),
    val blnIni: GenderCount = GenderCount(),
    // Dokumen
    val wajibKtpSudah: Int = 0,
    val wajibKtpBelum: Int = 0,
    val totalWajibKtp: Int = 0,
    val kkSudah: Int = 0,
    val kkBelum: Int = 0,
    val totalKk: Int = 0,
    val akteSudah: Int = 0,
    val akteBelum: Int = 0,
    val totalAkte: Int = 0,
    val kiaSudah: Int = 0,
    val kiaBelum: Int = 0,
    val totalKia: Int = 0
)

object LaporanBulananGenerator {

    val DUSUN_CONFIG_LIST = listOf(
        DusunConfig("CIBUBUAY", "RW 001", 1, listOf("001", "002", "003", "004", "005"), 1.29),
        DusunConfig("SUNDAWENANG", "RW 002", 2, listOf("006", "007", "008", "009"), 1.15),
        DusunConfig("CIMANGGU", "RW 003", 3, listOf("010", "011", "012", "013", "022"), 1.17),
        DusunConfig("MEKARLAKSANA", "RW 004", 4, listOf("014", "015", "016", "017"), 1.18),
        DusunConfig("MEKARJAYA", "RW 005", 5, listOf("018", "019", "020", "021"), 1.27)
    )

    val MONTH_NAMES = listOf(
        "JANUARI", "FEBRUARI", "MARET", "APRIL", "MEI", "JUNI",
        "JULI", "AGUSTUS", "SEPTEMBER", "OKTOBER", "NOVEMBER", "DESEMBER"
    )

    fun getLastDayOfMonth(monthIndex1to12: Int, year: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthIndex1to12 - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun getAutoTitimangsa(monthIndex1to12: Int, year: Int, kotaKab: String = "Tasikmalaya"): String {
        val lastDay = getLastDayOfMonth(monthIndex1to12, year)
        val monthName = MONTH_NAMES.getOrNull(monthIndex1to12 - 1) ?: "JUNI"
        val capitalizedMonth = monthName.lowercase().replaceFirstChar { it.uppercase() }
        return "$kotaKab, $lastDay $capitalizedMonth $year"
    }

    fun formatNumber(number: Int): String {
        val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
            groupingSeparator = '.'
        }
        val df = DecimalFormat("#,###", symbols)
        return df.format(number)
    }

    fun formatDecimal(number: Double): String {
        val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
            decimalSeparator = ','
        }
        val df = DecimalFormat("0.00", symbols)
        return df.format(number)
    }

    fun getDusunForPenduduk(p: Penduduk): String {
        val addr = p.alamat.uppercase().trim()
        val rwClean = p.rw.filter { it.isDigit() }.toIntOrNull() ?: 0
        val rtClean = p.rt.filter { it.isDigit() }.toIntOrNull() ?: 0

        // 1. Check direct Dusun name in address (exclude generic village name)
        when {
            addr.contains("CIBUBUAY") -> return "CIBUBUAY"
            addr.contains("SUNDAWENANG") -> return "SUNDAWENANG"
            addr.contains("MEKARLAKSANA") -> return "MEKARLAKSANA"
            addr.contains("MEKARJAYA") -> return "MEKARJAYA"
            addr.contains("DUSUN CIMANGGU") || addr.contains("KP. CIMANGGU") || addr.contains("KAMPUNG CIMANGGU") || addr.contains("KP CIMANGGU") -> return "CIMANGGU"
        }

        // 2. Check RW number (RW 001 - RW 005)
        when (rwClean) {
            1 -> return "CIBUBUAY"
            2 -> return "SUNDAWENANG"
            3 -> return "CIMANGGU"
            4 -> return "MEKARLAKSANA"
            5 -> return "MEKARJAYA"
        }

        // 3. Check RT number mapping of Desa Cimanggu
        when {
            rtClean in 1..5 -> return "CIBUBUAY"
            rtClean in 6..9 -> return "SUNDAWENANG"
            rtClean in 10..13 || rtClean == 22 -> return "CIMANGGU"
            rtClean in 14..17 -> return "MEKARLAKSANA"
            rtClean in 18..21 -> return "MEKARJAYA"
        }

        // 4. Check if address has any custom dusun identifier
        if (addr.isNotBlank() && !addr.startsWith("DESA") && !addr.startsWith("JL")) {
            val custom = addr.replace("DUSUN", "").replace("KP.", "").replace("KAMPUNG", "").trim()
            if (custom.isNotBlank() && custom.length >= 3) {
                val candidate = custom.split(",", " ", "-").firstOrNull { it.isNotBlank() }?.uppercase() ?: "CIMANGGU"
                if (candidate.contains("CIBUBUAY")) return "CIBUBUAY"
                if (candidate.contains("SUNDAWENANG")) return "SUNDAWENANG"
                if (candidate.contains("MEKARLAKSANA")) return "MEKARLAKSANA"
                if (candidate.contains("MEKARJAYA")) return "MEKARJAYA"
                if (candidate.contains("CIMANGGU")) return "CIMANGGU"
            }
        }

        // 5. Default fallback to CIMANGGU
        return "CIMANGGU"
    }

    // ==========================================
    // CALCULATOR FORMAT 1: KELOMPOK UMUR
    // ==========================================
    fun generateFormat1(
        allPenduduk: List<Penduduk>,
        wilayahTugasFilter: String = "SEMUA", // "SEMUA", "CIBUBUAY", "SUNDAWENANG", etc.
        viewMode: String = "DUSUN" // "DUSUN", "RT", "RW"
    ): Pair<List<Format1Row>, Format1Row> {
        val activePenduduk = allPenduduk.filter { it.isAktif() }
        val rows = mutableListOf<Format1Row>()

        if (wilayahTugasFilter != "SEMUA" && wilayahTugasFilter.isNotBlank()) {
            val dusunCfg = DUSUN_CONFIG_LIST.find { it.name.equals(wilayahTugasFilter, ignoreCase = true) }
            if (dusunCfg != null) {
                // Tampilkan rincian per RT di dusun tersebut
                val rtList = dusunCfg.rtList
                rtList.forEachIndexed { index, rtNum ->
                    val filtered = activePenduduk.filter {
                        getDusunForPenduduk(it).equals(dusunCfg.name, ignoreCase = true) &&
                                it.rt.filter { c -> c.isDigit() }.padStart(3, '0').takeLast(3) == rtNum
                    }
                    rows.add(buildFormat1Row(index + 1, "RT $rtNum / ${dusunCfg.rwLabel}", filtered))
                }
            } else {
                DUSUN_CONFIG_LIST.forEachIndexed { index, cfg ->
                    val filtered = activePenduduk.filter { getDusunForPenduduk(it).equals(cfg.name, ignoreCase = true) }
                    rows.add(buildFormat1Row(index + 1, cfg.name, filtered))
                }
            }
        } else {
            // Default: 5 Dusun se-Desa
            DUSUN_CONFIG_LIST.forEachIndexed { index, cfg ->
                val filtered = activePenduduk.filter { getDusunForPenduduk(it).equals(cfg.name, ignoreCase = true) }
                rows.add(buildFormat1Row(index + 1, cfg.name, filtered))
            }
        }

        // Calculate Grand Total Row
        val totalRow = Format1Row(
            no = 0,
            namaWilayah = if (wilayahTugasFilter != "SEMUA" && wilayahTugasFilter.isNotBlank()) "JUMLAH DUSUN $wilayahTugasFilter" else "JUMLAH",
            age0to5 = AgeRangeCount(rows.sumOf { it.age0to5.male }, rows.sumOf { it.age0to5.female }),
            age6to12 = AgeRangeCount(rows.sumOf { it.age6to12.male }, rows.sumOf { it.age6to12.female }),
            age13to15 = AgeRangeCount(rows.sumOf { it.age13to15.male }, rows.sumOf { it.age13to15.female }),
            age16to18 = AgeRangeCount(rows.sumOf { it.age16to18.male }, rows.sumOf { it.age16to18.female }),
            age19to24 = AgeRangeCount(rows.sumOf { it.age19to24.male }, rows.sumOf { it.age19to24.female }),
            age25to29 = AgeRangeCount(rows.sumOf { it.age25to29.male }, rows.sumOf { it.age25to29.female }),
            age30to34 = AgeRangeCount(rows.sumOf { it.age30to34.male }, rows.sumOf { it.age30to34.female }),
            age35to39 = AgeRangeCount(rows.sumOf { it.age35to39.male }, rows.sumOf { it.age35to39.female }),
            age40to44 = AgeRangeCount(rows.sumOf { it.age40to44.male }, rows.sumOf { it.age40to44.female }),
            age45to49 = AgeRangeCount(rows.sumOf { it.age45to49.male }, rows.sumOf { it.age45to49.female }),
            age50to54 = AgeRangeCount(rows.sumOf { it.age50to54.male }, rows.sumOf { it.age50to54.female }),
            age55to59 = AgeRangeCount(rows.sumOf { it.age55to59.male }, rows.sumOf { it.age55to59.female }),
            age60to64 = AgeRangeCount(rows.sumOf { it.age60to64.male }, rows.sumOf { it.age60to64.female }),
            age65to69 = AgeRangeCount(rows.sumOf { it.age65to69.male }, rows.sumOf { it.age65to69.female }),
            age70to74 = AgeRangeCount(rows.sumOf { it.age70to74.male }, rows.sumOf { it.age70to74.female }),
            age75Above = AgeRangeCount(rows.sumOf { it.age75Above.male }, rows.sumOf { it.age75Above.female }),
            totalMale = rows.sumOf { it.totalMale },
            totalFemale = rows.sumOf { it.totalFemale }
        )

        return Pair(rows, totalRow)
    }

    private fun buildFormat1Row(no: Int, label: String, list: List<Penduduk>): Format1Row {
        fun countAge(min: Int, max: Int): AgeRangeCount {
            val males = list.count { it.isMale() && it.getEffectiveAge() in min..max }
            val females = list.count { it.isFemale() && it.getEffectiveAge() in min..max }
            return AgeRangeCount(males, females)
        }

        fun countAge75Above(): AgeRangeCount {
            val males = list.count { it.isMale() && it.getEffectiveAge() >= 75 }
            val females = list.count { it.isFemale() && it.getEffectiveAge() >= 75 }
            return AgeRangeCount(males, females)
        }

        val totalMale = list.count { it.isMale() }
        val totalFemale = list.count { it.isFemale() }

        return Format1Row(
            no = no,
            namaWilayah = label,
            age0to5 = countAge(0, 5),
            age6to12 = countAge(6, 12),
            age13to15 = countAge(13, 15),
            age16to18 = countAge(16, 18),
            age19to24 = countAge(19, 24),
            age25to29 = countAge(25, 29),
            age30to34 = countAge(30, 34),
            age35to39 = countAge(35, 39),
            age40to44 = countAge(40, 44),
            age45to49 = countAge(45, 49),
            age50to54 = countAge(50, 54),
            age55to59 = countAge(55, 59),
            age60to64 = countAge(60, 64),
            age65to69 = countAge(65, 69),
            age70to74 = countAge(70, 74),
            age75Above = countAge75Above(),
            totalMale = totalMale,
            totalFemale = totalFemale
        )
    }

    // ==========================================
    // CALCULATOR FORMAT 2: PENDIDIKAN, PEKERJAAN, AGAMA, KEWARGANEGARAAN
    // ==========================================
    fun generateFormat2(
        allPenduduk: List<Penduduk>,
        wilayahTugasFilter: String = "SEMUA",
        viewMode: String = "DUSUN"
    ): Triple<List<Format2PendidikanPekerjaanRow>, Format2PendidikanPekerjaanRow, Pair<List<Format2AgamaKewarganegaraanRow>, Format2AgamaKewarganegaraanRow>> {
        val activePenduduk = allPenduduk.filter { it.isAktif() }

        val rowsTable1 = mutableListOf<Format2PendidikanPekerjaanRow>()
        val rowsTable2 = mutableListOf<Format2AgamaKewarganegaraanRow>()

        if (wilayahTugasFilter != "SEMUA" && wilayahTugasFilter.isNotBlank()) {
            val dusunCfg = DUSUN_CONFIG_LIST.find { it.name.equals(wilayahTugasFilter, ignoreCase = true) }
            if (dusunCfg != null) {
                // Per-RT breakdown in this Dusun
                dusunCfg.rtList.forEachIndexed { index, rtNum ->
                    val filtered = activePenduduk.filter {
                        getDusunForPenduduk(it).equals(dusunCfg.name, ignoreCase = true) &&
                                it.rt.filter { c -> c.isDigit() }.padStart(3, '0').takeLast(3) == rtNum
                    }
                    val (row1, row2) = computeFormat2ForList(index + 1, "RT $rtNum / ${dusunCfg.rwLabel}", filtered)
                    rowsTable1.add(row1)
                    rowsTable2.add(row2)
                }
            } else {
                DUSUN_CONFIG_LIST.forEachIndexed { index, cfg ->
                    val filtered = activePenduduk.filter { getDusunForPenduduk(it).equals(cfg.name, ignoreCase = true) }
                    val (row1, row2) = computeFormat2ForList(index + 1, cfg.name, filtered)
                    rowsTable1.add(row1)
                    rowsTable2.add(row2)
                }
            }
        } else {
            // Default 5 Dusun se-Desa
            DUSUN_CONFIG_LIST.forEachIndexed { index, cfg ->
                val filtered = activePenduduk.filter { getDusunForPenduduk(it).equals(cfg.name, ignoreCase = true) }
                val (row1, row2) = computeFormat2ForList(index + 1, cfg.name, filtered)
                rowsTable1.add(row1)
                rowsTable2.add(row2)
            }
        }

        val totalTable1 = Format2PendidikanPekerjaanRow(
            no = 0,
            namaWilayah = if (wilayahTugasFilter != "SEMUA" && wilayahTugasFilter.isNotBlank()) "JUMLAH DUSUN $wilayahTugasFilter" else "JUMLAH",
            belumSekolah = rowsTable1.sumOf { it.belumSekolah },
            tidakTamatSd = rowsTable1.sumOf { it.tidakTamatSd },
            tamatSd = rowsTable1.sumOf { it.tamatSd },
            tamatSmp = rowsTable1.sumOf { it.tamatSmp },
            tamatSma = rowsTable1.sumOf { it.tamatSma },
            diploma12 = rowsTable1.sumOf { it.diploma12 },
            diploma3 = rowsTable1.sumOf { it.diploma3 },
            diploma4S1 = rowsTable1.sumOf { it.diploma4S1 },
            strata2 = rowsTable1.sumOf { it.strata2 },
            strata3 = rowsTable1.sumOf { it.strata3 },
            totalPendidikan = rowsTable1.sumOf { it.totalPendidikan },
            pnsTniPolri = rowsTable1.sumOf { it.pnsTniPolri },
            karyawan = rowsTable1.sumOf { it.karyawan },
            buruh = rowsTable1.sumOf { it.buruh },
            petani = rowsTable1.sumOf { it.petani },
            peternak = rowsTable1.sumOf { it.peternak },
            nelayan = rowsTable1.sumOf { it.nelayan },
            wiraswasta = rowsTable1.sumOf { it.wiraswasta },
            pelajar = rowsTable1.sumOf { it.pelajar },
            belumBekerja = rowsTable1.sumOf { it.belumBekerja },
            lainnya = rowsTable1.sumOf { it.lainnya },
            totalPekerjaan = rowsTable1.sumOf { it.totalPekerjaan }
        )

        val totalTable2 = Format2AgamaKewarganegaraanRow(
            no = 0,
            namaWilayah = if (wilayahTugasFilter != "SEMUA" && wilayahTugasFilter.isNotBlank()) "JUMLAH DUSUN $wilayahTugasFilter" else "JUMLAH",
            islam = rowsTable2.sumOf { it.islam },
            kristen = rowsTable2.sumOf { it.kristen },
            hindu = rowsTable2.sumOf { it.hindu },
            budha = rowsTable2.sumOf { it.budha },
            khonghucu = rowsTable2.sumOf { it.khonghucu },
            kepercayaan = rowsTable2.sumOf { it.kepercayaan },
            totalAgama = rowsTable2.sumOf { it.totalAgama },
            wna = rowsTable2.sumOf { it.wna },
            wni = rowsTable2.sumOf { it.wni },
            totalKewarganegaraan = rowsTable2.sumOf { it.totalKewarganegaraan }
        )

        return Triple(rowsTable1, totalTable1, Pair(rowsTable2, totalTable2))
    }

    private fun computeFormat2ForList(
        no: Int,
        label: String,
        filtered: List<Penduduk>
    ): Pair<Format2PendidikanPekerjaanRow, Format2AgamaKewarganegaraanRow> {
        var belumSekolah = 0
        var tidakTamatSd = 0
        var tamatSd = 0
        var tamatSmp = 0
        var tamatSma = 0
        var diploma12 = 0
        var diploma3 = 0
        var diploma4S1 = 0
        var strata2 = 0
        var strata3 = 0

        var pns = 0
        var karyawan = 0
        var buruh = 0
        var petani = 0
        var peternak = 0
        var nelayan = 0
        var wiraswasta = 0
        var pelajar = 0
        var belumBekerja = 0
        var lainnya = 0

        var islam = 0
        var kristen = 0
        var hindu = 0
        var budha = 0
        var khonghucu = 0
        var kepercayaan = 0

        var wna = 0
        var wni = 0

        filtered.forEach { p ->
            val edu = p.pendidikanTerakhir.uppercase().trim()
            val age = p.getEffectiveAge()
            when {
                edu.contains("STRATA III") || edu.contains("S3") || edu.contains("DOKTOR") -> strata3++
                edu.contains("STRATA II") || edu.contains("S2") || edu.contains("MAGISTER") -> strata2++
                edu.contains("DIPLOMA IV") || edu.contains("D4") || edu.contains("STRATA I") || edu.contains("S1") || (edu.contains("SARJANA") && !edu.contains("MUDA")) -> diploma4S1++
                edu.contains("DIPLOMA III") || edu.contains("D3") || edu.contains("AKADEMI") || edu.contains("SARJANA MUDA") -> diploma3++
                edu.contains("DIPLOMA I") || edu.contains("DIPLOMA II") || edu.contains("D1") || edu.contains("D2") -> diploma12++
                edu.contains("SMA") || edu.contains("SMK") || edu.contains("SLTA") || edu.contains("MA") || (edu.contains("SEDERAJAT") && !edu.contains("SD") && !edu.contains("SMP")) -> tamatSma++
                edu.contains("SMP") || edu.contains("SLTP") || edu.contains("MTS") -> tamatSmp++
                edu.contains("TIDAK TAMAT SD") || edu.contains("BELUM TAMAT SD") || edu.contains("PUTUS SEKOLAH") -> tidakTamatSd++
                edu.contains("SD") || edu.contains("MI") -> tamatSd++
                edu.contains("BELUM") || edu.contains("TIDAK") || age < 6 || edu.isBlank() -> belumSekolah++
                else -> tamatSd++
            }

            val job = p.pekerjaan.uppercase().trim()
            when {
                job.contains("PNS") || job.contains("TNI") || job.contains("POLRI") || job.contains("ASN") || job.contains("PEGAWAI NEGERI") -> pns++
                job.contains("KARYAWAN") || job.contains("HONORER") || job.contains("BUMN") || job.contains("BUMD") || (job.contains("SWASTA") && !job.contains("WIRASWASTA")) || job.contains("PERANGKAT DESA") -> karyawan++
                job.contains("BURUH") || job.contains("TUKANG") || job.contains("KULI") || job.contains("SOPIR") || job.contains("PEMBANTU") -> buruh++
                job.contains("PETANI") || job.contains("PEKEBUN") || job.contains("TANI") || job.contains("SAWAH") -> petani++
                job.contains("PETERNAK") || job.contains("TERNAK") -> peternak++
                job.contains("NELAYAN") || job.contains("PERIKANAN") || job.contains("TAMBAK") -> nelayan++
                job.contains("WIRASWASTA") || job.contains("PEDAGANG") || job.contains("PENGUSAHA") || job.contains("DAGANG") || job.contains("BISNIS") || job.contains("JASA") -> wiraswasta++
                job.contains("PELAJAR") || job.contains("MAHASISWA") || job.contains("SANTRI") || job.contains("SISWA") -> pelajar++
                job.contains("BELUM") || job.contains("TIDAK BEKERJA") || job.contains("MENGANGGUR") || job.contains("IRT") || job.contains("IBU RUMAH TANGGA") || (age < 6 && job.isBlank()) -> belumBekerja++
                job.isBlank() || job == "-" -> if (age < 18) pelajar++ else belumBekerja++
                else -> lainnya++
            }

            val rel = p.agama.uppercase().trim()
            when {
                rel.contains("ISLAM") -> islam++
                rel.contains("KRISTEN") || rel.contains("KATOLIK") || rel.contains("PROTESTAN") -> kristen++
                rel.contains("HINDU") -> hindu++
                rel.contains("BUD") -> budha++
                rel.contains("KONG") || rel.contains("KHONG") -> khonghucu++
                rel.contains("KEPERCAYAAN") -> kepercayaan++
                else -> islam++
            }

            val kw = p.kewarganegaraan.uppercase().trim()
            if (kw == "WNA") wna++ else wni++
        }

        val totalEdu = belumSekolah + tidakTamatSd + tamatSd + tamatSmp + tamatSma + diploma12 + diploma3 + diploma4S1 + strata2 + strata3
        val totalJob = pns + karyawan + buruh + petani + peternak + nelayan + wiraswasta + pelajar + belumBekerja + lainnya
        val totalAgama = islam + kristen + hindu + budha + khonghucu + kepercayaan
        val totalKw = wna + wni

        val row1 = Format2PendidikanPekerjaanRow(
            no = no,
            namaWilayah = label,
            belumSekolah = belumSekolah,
            tidakTamatSd = tidakTamatSd,
            tamatSd = tamatSd,
            tamatSmp = tamatSmp,
            tamatSma = tamatSma,
            diploma12 = diploma12,
            diploma3 = diploma3,
            diploma4S1 = diploma4S1,
            strata2 = strata2,
            strata3 = strata3,
            totalPendidikan = totalEdu,
            pnsTniPolri = pns,
            karyawan = karyawan,
            buruh = buruh,
            petani = petani,
            peternak = peternak,
            nelayan = nelayan,
            wiraswasta = wiraswasta,
            pelajar = pelajar,
            belumBekerja = belumBekerja,
            lainnya = lainnya,
            totalPekerjaan = totalJob
        )

        val row2 = Format2AgamaKewarganegaraanRow(
            no = no,
            namaWilayah = label,
            islam = islam,
            kristen = kristen,
            hindu = hindu,
            budha = budha,
            khonghucu = khonghucu,
            kepercayaan = kepercayaan,
            totalAgama = totalAgama,
            wna = wna,
            wni = wni,
            totalKewarganegaraan = totalKw
        )

        return Pair(row1, row2)
    }

    // ==========================================
    // CALCULATOR FORMAT 3: REKAPITULASI MUTASI & ADMINISTRASI
    // ==========================================
    fun generateFormat3(
        allPenduduk: List<Penduduk>,
        monthIndex1to12: Int,
        year: Int,
        wilayahTugasFilter: String = "SEMUA"
    ): Pair<List<Format3Row>, Format3Row> {
        val rows = mutableListOf<Format3Row>()

        if (wilayahTugasFilter != "SEMUA" && wilayahTugasFilter.isNotBlank()) {
            val dusunCfg = DUSUN_CONFIG_LIST.find { it.name.equals(wilayahTugasFilter, ignoreCase = true) }
            if (dusunCfg != null) {
                val rtList = dusunCfg.rtList
                val luasPerRt = if (rtList.isNotEmpty()) dusunCfg.luasKm / rtList.size else dusunCfg.luasKm
                rtList.forEachIndexed { index, rtNum ->
                    val filteredAll = allPenduduk.filter {
                        getDusunForPenduduk(it).equals(dusunCfg.name, ignoreCase = true) &&
                                it.rt.filter { c -> c.isDigit() }.padStart(3, '0').takeLast(3) == rtNum
                    }
                    rows.add(
                        computeFormat3Row(
                            no = index + 1,
                            label = "RT $rtNum / ${dusunCfg.rwLabel}",
                            luasKm = luasPerRt,
                            rtCount = 1,
                            rwCount = 1,
                            dusunCount = 0,
                            pendudukList = filteredAll,
                            monthIndex1to12 = monthIndex1to12,
                            year = year
                        )
                    )
                }
            } else {
                DUSUN_CONFIG_LIST.forEachIndexed { index, cfg ->
                    val allInDusun = allPenduduk.filter { getDusunForPenduduk(it).equals(cfg.name, ignoreCase = true) }
                    rows.add(
                        computeFormat3Row(
                            no = index + 1,
                            label = cfg.name,
                            luasKm = cfg.luasKm,
                            rtCount = cfg.rtList.size,
                            rwCount = 1,
                            dusunCount = 1,
                            pendudukList = allInDusun,
                            monthIndex1to12 = monthIndex1to12,
                            year = year
                        )
                    )
                }
            }
        } else {
            DUSUN_CONFIG_LIST.forEachIndexed { index, cfg ->
                val allInDusun = allPenduduk.filter { getDusunForPenduduk(it).equals(cfg.name, ignoreCase = true) }
                rows.add(
                    computeFormat3Row(
                        no = index + 1,
                        label = cfg.name,
                        luasKm = cfg.luasKm,
                        rtCount = cfg.rtList.size,
                        rwCount = 1,
                        dusunCount = 1,
                        pendudukList = allInDusun,
                        monthIndex1to12 = monthIndex1to12,
                        year = year
                    )
                )
            }
        }

        val totalRow = Format3Row(
            no = 0,
            namaWilayah = if (wilayahTugasFilter != "SEMUA" && wilayahTugasFilter.isNotBlank()) "JUMLAH DUSUN $wilayahTugasFilter" else "JUMLAH",
            luasWilayahKm = rows.sumOf { it.luasWilayahKm },
            jumlahRt = rows.sumOf { it.jumlahRt },
            jumlahRw = rows.sumOf { it.jumlahRw },
            jumlahDusun = rows.sumOf { it.jumlahDusun },
            blnLalu = GenderCount(rows.sumOf { it.blnLalu.male }, rows.sumOf { it.blnLalu.female }),
            lahirBlnIni = GenderCount(rows.sumOf { it.lahirBlnIni.male }, rows.sumOf { it.lahirBlnIni.female }),
            matiBlnIni = GenderCount(rows.sumOf { it.matiBlnIni.male }, rows.sumOf { it.matiBlnIni.female }),
            datangBlnIni = GenderCount(rows.sumOf { it.datangBlnIni.male }, rows.sumOf { it.datangBlnIni.female }),
            pindahBlnIni = GenderCount(rows.sumOf { it.pindahBlnIni.male }, rows.sumOf { it.pindahBlnIni.female }),
            blnIni = GenderCount(rows.sumOf { it.blnIni.male }, rows.sumOf { it.blnIni.female }),
            wajibKtpSudah = rows.sumOf { it.wajibKtpSudah },
            wajibKtpBelum = rows.sumOf { it.wajibKtpBelum },
            totalWajibKtp = rows.sumOf { it.totalWajibKtp },
            kkSudah = rows.sumOf { it.kkSudah },
            kkBelum = rows.sumOf { it.kkBelum },
            totalKk = rows.sumOf { it.totalKk },
            akteSudah = rows.sumOf { it.akteSudah },
            akteBelum = rows.sumOf { it.akteBelum },
            totalAkte = rows.sumOf { it.totalAkte },
            kiaSudah = rows.sumOf { it.kiaSudah },
            kiaBelum = rows.sumOf { it.kiaBelum },
            totalKia = rows.sumOf { it.totalKia }
        )

        return Pair(rows, totalRow)
    }

    private fun computeFormat3Row(
        no: Int,
        label: String,
        luasKm: Double,
        rtCount: Int,
        rwCount: Int,
        dusunCount: Int,
        pendudukList: List<Penduduk>,
        monthIndex1to12: Int,
        year: Int
    ): Format3Row {
        val activeList = pendudukList.filter { it.isAktif() }

        val lahirBlnIniMale = activeList.count {
            it.isMale() && isMatchingMonthYear(it.tanggalLahir, monthIndex1to12, year)
        }
        val lahirBlnIniFemale = activeList.count {
            it.isFemale() && isMatchingMonthYear(it.tanggalLahir, monthIndex1to12, year)
        }

        val matiBlnIniMale = pendudukList.count {
            it.isMeninggal() && it.isMale() && isMatchingMonthYear(it.tanggalKematian, monthIndex1to12, year)
        }
        val matiBlnIniFemale = pendudukList.count {
            it.isMeninggal() && it.isFemale() && isMatchingMonthYear(it.tanggalKematian, monthIndex1to12, year)
        }

        val pindahBlnIniMale = pendudukList.count {
            it.isPindah() && it.isMale() && isMatchingMonthYear(it.tanggalPindah, monthIndex1to12, year)
        }
        val pindahBlnIniFemale = pendudukList.count {
            it.isPindah() && it.isFemale() && isMatchingMonthYear(it.tanggalPindah, monthIndex1to12, year)
        }

        val datangBlnIniMale = 0
        val datangBlnIniFemale = 0

        val blnIniMale = activeList.count { it.isMale() }
        val blnIniFemale = activeList.count { it.isFemale() }

        // Penduduk Bln Lalu = Bln Ini - Lahir - Datang + Mati + Pindah
        val blnLaluMale = maxOf(0, blnIniMale - lahirBlnIniMale - datangBlnIniMale + matiBlnIniMale + pindahBlnIniMale)
        val blnLaluFemale = maxOf(0, blnIniFemale - lahirBlnIniFemale - datangBlnIniFemale + matiBlnIniFemale + pindahBlnIniFemale)

        // Administrasi
        val wajibKtpList = activeList.filter { it.getEffectiveAge() >= 17 || !it.statusPerkawinan.equals("BELUM KAWIN", ignoreCase = true) }
        val wajibKtpSudah = wajibKtpList.count { it.kepemilikanEKtp.equals("SUDAH MEMILIKI", ignoreCase = true) }
        val wajibKtpBelum = wajibKtpList.size - wajibKtpSudah

        val totalKk = activeList.mapNotNull { it.noKk.takeIf { kk -> kk.isNotBlank() } }.distinct().size
            .let { if (it > 0) it else activeList.count { p -> p.shdk.equals("KEPALA KELUARGA", ignoreCase = true) } }

        val akteSudah = activeList.count { it.kepemilikanAktaKelahiran.equals("ADA", ignoreCase = true) }
        val akteBelum = activeList.size - akteSudah

        val anakList = activeList.filter { it.getEffectiveAge() in 0..16 }
        val kiaSudah = anakList.count { it.kartuKia.equals("ADA", ignoreCase = true) }
        val kiaBelum = anakList.size - kiaSudah

        return Format3Row(
            no = no,
            namaWilayah = label,
            luasWilayahKm = luasKm,
            jumlahRt = rtCount,
            jumlahRw = rwCount,
            jumlahDusun = dusunCount,
            blnLalu = GenderCount(blnLaluMale, blnLaluFemale),
            lahirBlnIni = GenderCount(lahirBlnIniMale, lahirBlnIniFemale),
            matiBlnIni = GenderCount(matiBlnIniMale, matiBlnIniFemale),
            datangBlnIni = GenderCount(datangBlnIniMale, datangBlnIniFemale),
            pindahBlnIni = GenderCount(pindahBlnIniMale, pindahBlnIniFemale),
            blnIni = GenderCount(blnIniMale, blnIniFemale),
            wajibKtpSudah = wajibKtpSudah,
            wajibKtpBelum = wajibKtpBelum,
            totalWajibKtp = wajibKtpList.size,
            kkSudah = totalKk,
            kkBelum = 0,
            totalKk = totalKk,
            akteSudah = akteSudah,
            akteBelum = akteBelum,
            totalAkte = activeList.size,
            kiaSudah = kiaSudah,
            kiaBelum = kiaBelum,
            totalKia = anakList.size
        )
    }

    private fun isMatchingMonthYear(dateString: String, month: Int, year: Int): Boolean {
        if (dateString.isBlank()) return false
        val clean = dateString.trim().take(10)
        val parts = clean.split("-", "/", ".")
        if (parts.size != 3) return false
        val p0 = parts[0].toIntOrNull() ?: 0
        val p1 = parts[1].toIntOrNull() ?: 0
        val p2 = parts[2].toIntOrNull() ?: 0

        val dYear: Int
        val dMonth: Int
        if (p0 > 1000) {
            dYear = p0
            dMonth = p1
        } else if (p2 > 1000) {
            dYear = p2
            dMonth = p1
        } else {
            return false
        }
        return dYear == year && dMonth == month
    }

    // ==========================================
    // SHARE TEXT GENERATOR (WhatsApp / Pesan)
    // ==========================================
    fun generateShareText(
        formatType: Int, // 1, 2, or 3
        profile: UserProfile,
        monthIndex1to12: Int,
        year: Int,
        titimangsa: String,
        allPenduduk: List<Penduduk>,
        wilayahTugasFilter: String = "SEMUA"
    ): String {
        val monthName = MONTH_NAMES.getOrNull(monthIndex1to12 - 1) ?: "JUNI"
        val sb = StringBuilder()

        sb.append("📋 *LAPORAN DATA KEPENDUDUKAN*\n")
        sb.append("*${profile.namaDesa.uppercase()} KECAMATAN ${profile.kecamatan.uppercase()} KABUPATEN ${profile.kabupaten.uppercase()}*\n")
        sb.append("*BULAN : $monthName $year*\n")
        sb.append("────────────────────────\n")

        when (formatType) {
            1 -> {
                sb.append("📊 *FORMAT 1: DISTRIBUSI KELOMPOK UMUR*\n\n")
                val (rows, total) = generateFormat1(allPenduduk, wilayahTugasFilter)
                rows.forEach { r ->
                    sb.append("📍 *${r.no}. ${r.namaWilayah}*\n")
                    sb.append("   • 0-5 Th: LK ${r.age0to5.male}, PR ${r.age0to5.female} (Jml: ${r.age0to5.total})\n")
                    sb.append("   • 6-12 Th: LK ${r.age6to12.male}, PR ${r.age6to12.female} (Jml: ${r.age6to12.total})\n")
                    sb.append("   • 13-18 Th: LK ${r.age13to15.male + r.age16to18.male}, PR ${r.age13to15.female + r.age16to18.female}\n")
                    sb.append("   • 19-59 Th (Produktif): LK ${r.age19to24.male + r.age25to29.male + r.age30to34.male + r.age35to39.male + r.age40to44.male + r.age45to49.male + r.age50to54.male + r.age55to59.male}, PR ${r.age19to24.female + r.age25to29.female + r.age30to34.female + r.age35to39.female + r.age40to44.female + r.age45to49.female + r.age50to54.female + r.age55to59.female}\n")
                    sb.append("   • 60+ Th (Lansia): LK ${r.age60to64.male + r.age65to69.male + r.age70to74.male + r.age75Above.male}, PR ${r.age60to64.female + r.age65to69.female + r.age70to74.female + r.age75Above.female}\n")
                    sb.append("   👉 *Total Dusun: LK ${r.totalMale}, PR ${r.totalFemale} = ${r.grandTotal} Jiwa*\n\n")
                }
                sb.append("────────────────────────\n")
                sb.append("🏆 *TOTAL KESELURUHAN DESA:*\n")
                sb.append("• Laki-laki: ${formatNumber(total.totalMale)} Jiwa\n")
                sb.append("• Perempuan: ${formatNumber(total.totalFemale)} Jiwa\n")
                sb.append("• Grand Total: *${formatNumber(total.grandTotal)} Jiwa*\n")
            }
            2 -> {
                sb.append("🎓 *FORMAT 2: PENDIDIKAN, PEKERJAAN, AGAMA & KEWARGANEGARAAN*\n\n")
                val (t1, total1, t2Pair) = generateFormat2(allPenduduk, wilayahTugasFilter)
                val (_, total2) = t2Pair
                sb.append("📚 *Rekap Pendidikan:* \n")
                sb.append("• Belum Sekolah: ${total1.belumSekolah}\n")
                sb.append("• SD/Sederajat: ${total1.tamatSd}\n")
                sb.append("• SMP/Sederajat: ${total1.tamatSmp}\n")
                sb.append("• SMA/Sederajat: ${total1.tamatSma}\n")
                sb.append("• Diploma / Sarjana (D1-S1): ${total1.diploma12 + total1.diploma3 + total1.diploma4S1}\n")
                sb.append("• Pascasarjana (S2-S3): ${total1.strata2 + total1.strata3}\n\n")

                sb.append("💼 *Rekap Pekerjaan:* \n")
                sb.append("• Petani/Peternak/Nelayan: ${total1.petani + total1.peternak + total1.nelayan}\n")
                sb.append("• Buruh: ${total1.buruh}\n")
                sb.append("• Wiraswasta/Pedagang: ${total1.wiraswasta}\n")
                sb.append("• Karyawan Swasta/BUMN: ${total1.karyawan}\n")
                sb.append("• PNS/TNI/POLRI: ${total1.pnsTniPolri}\n")
                sb.append("• Pelajar/Mahasiswa: ${total1.pelajar}\n")
                sb.append("• Belum Bekerja / IRT / Lainnya: ${total1.belumBekerja + total1.lainnya}\n\n")

                sb.append("🕌 *Rekap Agama:* \n")
                sb.append("• Islam: ${total2.islam}, Kristen: ${total2.kristen}, Hindu: ${total2.hindu}, Budha: ${total2.budha}, Khonghucu: ${total2.khonghucu}\n\n")

                sb.append("🇮🇩 *Kewarganegaraan:* \n")
                sb.append("• WNI: ${total2.wni} Jiwa | WNA: ${total2.wna} Jiwa\n")
            }
            3 -> {
                sb.append("📋 *FORMAT 3: MUTASI & REKAP ADMINISTRASI*\n\n")
                val (rows, total) = generateFormat3(allPenduduk, monthIndex1to12, year, wilayahTugasFilter)
                rows.forEach { r ->
                    sb.append("📍 *${r.no}. ${r.namaWilayah}* (Luas: ${formatDecimal(r.luasWilayahKm)} Km², ${r.jumlahRt} RT / ${r.jumlahRw} RW)\n")
                    sb.append("   • Penduduk Bln Lalu: LK ${r.blnLalu.male}, PR ${r.blnLalu.female} = ${r.blnLalu.total}\n")
                    sb.append("   • Lahir Bln Ini: ${r.lahirBlnIni.total} | Mati Bln Ini: ${r.matiBlnIni.total}\n")
                    sb.append("   • Datang Bln Ini: ${r.datangBlnIni.total} | Pindah Bln Ini: ${r.pindahBlnIni.total}\n")
                    sb.append("   • Penduduk Bln Ini: *LK ${r.blnIni.male}, PR ${r.blnIni.female} = ${r.blnIni.total} Jiwa*\n")
                    sb.append("   • Wajib KTP: ${r.wajibKtpSudah} Ada / ${r.wajibKtpBelum} Belum (Total: ${r.totalWajibKtp})\n")
                    sb.append("   • KK: ${r.totalKk} KK | Akta Lahir: ${r.akteSudah} Ada | KIA: ${r.kiaSudah} Ada\n\n")
                }
                sb.append("────────────────────────\n")
                sb.append("🏆 *TOTAL REKAP DESA:*\n")
                sb.append("• Luas Wilayah: ${formatDecimal(total.luasWilayahKm)} Km² (22 RT, 5 RW, 5 Dusun)\n")
                sb.append("• Penduduk Bln Ini: *LK ${formatNumber(total.blnIni.male)}, PR ${formatNumber(total.blnIni.female)} = ${formatNumber(total.blnIni.total)} Jiwa*\n")
                sb.append("• Mutasi: Lahir +${total.lahirBlnIni.total}, Mati -${total.matiBlnIni.total}, Pindah -${total.pindahBlnIni.total}\n")
                sb.append("• Administrasi: ${formatNumber(total.totalKk)} KK, ${formatNumber(total.wajibKtpSudah)} E-KTP, ${formatNumber(total.akteSudah)} Akta Lahir, ${formatNumber(total.kiaSudah)} KIA\n")
            }
        }

        sb.append("\n────────────────────────\n")
        sb.append("Mengetahui,\n")
        sb.append("Kepala Desa ${profile.namaDesa.replace("Desa ", "")}: *${profile.namaKades}*\n\n")
        sb.append("$titimangsa\n")
        sb.append("Kasi Pemerintahan: *${profile.namaPetugas}*\n")
        sb.append("NIP: ${profile.nipPetugas}\n")

        return sb.toString()
    }

    // ==========================================
    // HTML GENERATOR FOR NATIVE PRINTING & PDF
    // ==========================================
    fun generateHtmlReport(
        formatType: Int,
        profile: UserProfile,
        monthIndex1to12: Int,
        year: Int,
        titimangsa: String,
        allPenduduk: List<Penduduk>,
        wilayahTugasFilter: String = "SEMUA"
    ): String {
        val monthName = MONTH_NAMES.getOrNull(monthIndex1to12 - 1) ?: "JUNI"

        val headerHtml = """
            <div style="text-align: center; margin-bottom: 14px;">
                <h2 style="margin: 0; font-size: 14pt; font-weight: bold; text-transform: uppercase; letter-spacing: 0.5px;">LAPORAN DATA KEPENDUDUKAN</h2>
                <h3 style="margin: 3px 0; font-size: 13pt; font-weight: bold; text-transform: uppercase;">${profile.namaDesa.uppercase()} KECAMATAN ${profile.kecamatan.uppercase()} KABUPATEN ${profile.kabupaten.uppercase()}</h3>
                <h4 style="margin: 3px 0; font-size: 12pt; font-weight: bold;">BULAN : $monthName $year</h4>
            </div>
        """.trimIndent()

        val signatureHtml = """
            <div style="margin-top: 25px; width: 100%; display: flex; justify-content: space-between; font-size: 10pt; page-break-inside: avoid;">
                <div style="width: 40%; text-align: center;">
                    <p style="margin: 0;">Mengetahui;</p>
                    <p style="margin: 0; font-weight: bold;">Kepala ${profile.namaDesa}</p>
                    <div style="height: 60px;"></div>
                    <p style="margin: 0; font-weight: bold; text-decoration: underline; letter-spacing: 1px;">${profile.namaKades}</p>
                </div>
                <div style="width: 40%; text-align: center;">
                    <p style="margin: 0;">$titimangsa</p>
                    <p style="margin: 0; font-weight: bold;">Kasi Pemerintahan</p>
                    <div style="height: 60px;"></div>
                    <p style="margin: 0; font-weight: bold; text-decoration: underline;">${profile.namaPetugas}</p>
                    <p style="margin: 2px 0 0 0; font-size: 9pt;">NIP : ${profile.nipPetugas}</p>
                </div>
            </div>
        """.trimIndent()

        val css = """
            <style>
                @page { size: landscape; margin: 8mm; }
                body { font-family: 'Segoe UI', Arial, sans-serif; font-size: 9pt; color: #111; margin: 0; padding: 10px; background: #fff; }
                table { width: 100%; border-collapse: collapse; margin-bottom: 12px; font-size: 8.5pt; table-layout: auto; }
                th, td { border: 1px solid #000; padding: 4px 3px; text-align: center; }
                th { background-color: #f2f2f2; font-weight: bold; }
                .text-left { text-align: left; padding-left: 6px; }
                .bold { font-weight: bold; }
                .bg-total { background-color: #e9ecef; font-weight: bold; }
                .notes { font-size: 8pt; margin-top: 10px; line-height: 1.4; }
            </style>
        """.trimIndent()

        val tableContent = when (formatType) {
            1 -> {
                val (rows, total) = generateFormat1(allPenduduk, wilayahTugasFilter)
                """
                <table>
                    <thead>
                        <tr>
                            <th rowspan="2" style="width: 25px;">NO</th>
                            <th rowspan="2" style="min-width: 100px;">DUSUN</th>
                            <th colspan="2">0-5 TH</th>
                            <th colspan="2">6-12 TH</th>
                            <th colspan="2">13-15 TH</th>
                            <th colspan="2">16-18 TH</th>
                            <th colspan="2">19-24 TH</th>
                            <th colspan="2">25-29 TH</th>
                            <th colspan="2">30-34 TH</th>
                            <th colspan="2">35-39 TH</th>
                            <th colspan="2">40-44 TH</th>
                            <th colspan="2">45-49 TH</th>
                            <th colspan="2">50-54 TH</th>
                            <th colspan="2">55-59 TH</th>
                            <th colspan="2">60-64 TH</th>
                            <th colspan="2">65-69 TH</th>
                            <th colspan="2">70-74 TH</th>
                            <th colspan="2">75 TH KE ATAS</th>
                            <th colspan="2">JUMLAH</th>
                            <th rowspan="2">Jumlah Total</th>
                        </tr>
                        <tr>
                            <th>LK</th><th>PR</th>
                            <th>LK</th><th>PR</th>
                            <th>LK</th><th>PR</th>
                            <th>LK</th><th>PR</th>
                            <th>LK</th><th>PR</th>
                            <th>LK</th><th>PR</th>
                            <th>LK</th><th>PR</th>
                            <th>LK</th><th>PR</th>
                            <th>LK</th><th>PR</th>
                            <th>LK</th><th>PR</th>
                            <th>LK</th><th>PR</th>
                            <th>LK</th><th>PR</th>
                            <th>LK</th><th>PR</th>
                            <th>LK</th><th>PR</th>
                            <th>LK</th><th>PR</th>
                            <th>LK</th><th>PR</th>
                            <th>LK</th><th>PR</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${rows.joinToString("") { r ->
                            """
                            <tr>
                                <td>${r.no}</td>
                                <td class="text-left">${r.namaWilayah}</td>
                                <td>${r.age0to5.male}</td><td>${r.age0to5.female}</td>
                                <td>${r.age6to12.male}</td><td>${r.age6to12.female}</td>
                                <td>${r.age13to15.male}</td><td>${r.age13to15.female}</td>
                                <td>${r.age16to18.male}</td><td>${r.age16to18.female}</td>
                                <td>${r.age19to24.male}</td><td>${r.age19to24.female}</td>
                                <td>${r.age25to29.male}</td><td>${r.age25to29.female}</td>
                                <td>${r.age30to34.male}</td><td>${r.age30to34.female}</td>
                                <td>${r.age35to39.male}</td><td>${r.age35to39.female}</td>
                                <td>${r.age40to44.male}</td><td>${r.age40to44.female}</td>
                                <td>${r.age45to49.male}</td><td>${r.age45to49.female}</td>
                                <td>${r.age50to54.male}</td><td>${r.age50to54.female}</td>
                                <td>${r.age55to59.male}</td><td>${r.age55to59.female}</td>
                                <td>${r.age60to64.male}</td><td>${r.age60to64.female}</td>
                                <td>${r.age65to69.male}</td><td>${r.age65to69.female}</td>
                                <td>${r.age70to74.male}</td><td>${r.age70to74.female}</td>
                                <td>${r.age75Above.male}</td><td>${r.age75Above.female}</td>
                                <td class="bold">${formatNumber(r.totalMale)}</td><td class="bold">${formatNumber(r.totalFemale)}</td>
                                <td class="bold">${formatNumber(r.grandTotal)}</td>
                            </tr>
                            """
                        }}
                        <tr class="bg-total">
                            <td colspan="2" class="bold">JUMLAH</td>
                            <td>${total.age0to5.male}</td><td>${total.age0to5.female}</td>
                            <td>${total.age6to12.male}</td><td>${total.age6to12.female}</td>
                            <td>${total.age13to15.male}</td><td>${total.age13to15.female}</td>
                            <td>${total.age16to18.male}</td><td>${total.age16to18.female}</td>
                            <td>${total.age19to24.male}</td><td>${total.age19to24.female}</td>
                            <td>${total.age25to29.male}</td><td>${total.age25to29.female}</td>
                            <td>${total.age30to34.male}</td><td>${total.age30to34.female}</td>
                            <td>${total.age35to39.male}</td><td>${total.age35to39.female}</td>
                            <td>${total.age40to44.male}</td><td>${total.age40to44.female}</td>
                            <td>${total.age45to49.male}</td><td>${total.age45to49.female}</td>
                            <td>${total.age50to54.male}</td><td>${total.age50to54.female}</td>
                            <td>${total.age55to59.male}</td><td>${total.age55to59.female}</td>
                            <td>${total.age60to64.male}</td><td>${total.age60to64.female}</td>
                            <td>${total.age65to69.male}</td><td>${total.age65to69.female}</td>
                            <td>${total.age70to74.male}</td><td>${total.age70to74.female}</td>
                            <td>${total.age75Above.male}</td><td>${total.age75Above.female}</td>
                            <td class="bold">${formatNumber(total.totalMale)}</td><td class="bold">${formatNumber(total.totalFemale)}</td>
                            <td class="bold">${formatNumber(total.grandTotal)}</td>
                        </tr>
                    </tbody>
                </table>
                """
            }
            2 -> {
                val (t1, total1, t2Pair) = generateFormat2(allPenduduk, wilayahTugasFilter)
                val (rows2, total2) = t2Pair
                """
                <table>
                    <thead>
                        <tr>
                            <th rowspan="3" style="width: 25px;">NO</th>
                            <th rowspan="3" style="min-width: 90px;">DUSUN</th>
                            <th colspan="22">KONDISI</th>
                        </tr>
                        <tr>
                            <th colspan="11">MENURUT JENJANG PENDIDIKAN</th>
                            <th colspan="11">MATA PENCAHARIAN</th>
                        </tr>
                        <tr>
                            <th>TIDAK/<br>BELUM</th>
                            <th>TIDAK<br>TAMAT SD</th>
                            <th>TAMAT SD/<br>SEDERAJAT</th>
                            <th>TAMAT SMP/<br>SEDERAJAT</th>
                            <th>TAMAT SMA/<br>SEDERAJAT</th>
                            <th>TAMAT<br>D1/D2</th>
                            <th>TAMAT<br>D3/SM</th>
                            <th>TAMAT<br>D4/S1</th>
                            <th>TAMAT<br>S2</th>
                            <th>TAMAT<br>S3</th>
                            <th>JUMLAH</th>

                            <th>PNS,<br>TNI/POLRI</th>
                            <th>KARYAWAN</th>
                            <th>BURUH</th>
                            <th>PETANI</th>
                            <th>PETERNAK</th>
                            <th>NELAYAN</th>
                            <th>WIRASWASTA</th>
                            <th>PELAJAR/<br>MHS</th>
                            <th>BELUM<br>BEKERJA</th>
                            <th>LAINNYA</th>
                            <th>JUMLAH</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${t1.joinToString("") { r ->
                            """
                            <tr>
                                <td>${r.no}</td>
                                <td class="text-left">${r.namaWilayah}</td>
                                <td>${if (r.belumSekolah > 0) r.belumSekolah else "-"}</td>
                                <td>${if (r.tidakTamatSd > 0) r.tidakTamatSd else "-"}</td>
                                <td>${if (r.tamatSd > 0) r.tamatSd else "-"}</td>
                                <td>${if (r.tamatSmp > 0) r.tamatSmp else "-"}</td>
                                <td>${if (r.tamatSma > 0) r.tamatSma else "-"}</td>
                                <td>${if (r.diploma12 > 0) r.diploma12 else "-"}</td>
                                <td>${if (r.diploma3 > 0) r.diploma3 else "-"}</td>
                                <td>${if (r.diploma4S1 > 0) r.diploma4S1 else "-"}</td>
                                <td>${if (r.strata2 > 0) r.strata2 else "-"}</td>
                                <td>${if (r.strata3 > 0) r.strata3 else "-"}</td>
                                <td class="bold">${formatNumber(r.totalPendidikan)}</td>

                                <td>${if (r.pnsTniPolri > 0) r.pnsTniPolri else "-"}</td>
                                <td>${if (r.karyawan > 0) r.karyawan else "-"}</td>
                                <td>${if (r.buruh > 0) r.buruh else "-"}</td>
                                <td>${if (r.petani > 0) r.petani else "-"}</td>
                                <td>${if (r.peternak > 0) r.peternak else "-"}</td>
                                <td>${if (r.nelayan > 0) r.nelayan else "-"}</td>
                                <td>${if (r.wiraswasta > 0) r.wiraswasta else "-"}</td>
                                <td>${if (r.pelajar > 0) r.pelajar else "-"}</td>
                                <td>${if (r.belumBekerja > 0) r.belumBekerja else "-"}</td>
                                <td>${if (r.lainnya > 0) r.lainnya else "-"}</td>
                                <td class="bold">${formatNumber(r.totalPekerjaan)}</td>
                            </tr>
                            """
                        }}
                        <tr class="bg-total">
                            <td colspan="2" class="bold">JUMLAH</td>
                            <td>${formatNumber(total1.belumSekolah)}</td>
                            <td>${formatNumber(total1.tidakTamatSd)}</td>
                            <td>${formatNumber(total1.tamatSd)}</td>
                            <td>${formatNumber(total1.tamatSmp)}</td>
                            <td>${formatNumber(total1.tamatSma)}</td>
                            <td>${formatNumber(total1.diploma12)}</td>
                            <td>${formatNumber(total1.diploma3)}</td>
                            <td>${formatNumber(total1.diploma4S1)}</td>
                            <td>${formatNumber(total1.strata2)}</td>
                            <td>${formatNumber(total1.strata3)}</td>
                            <td class="bold">${formatNumber(total1.totalPendidikan)}</td>

                            <td>${formatNumber(total1.pnsTniPolri)}</td>
                            <td>${formatNumber(total1.karyawan)}</td>
                            <td>${formatNumber(total1.buruh)}</td>
                            <td>${formatNumber(total1.petani)}</td>
                            <td>${formatNumber(total1.peternak)}</td>
                            <td>${formatNumber(total1.nelayan)}</td>
                            <td>${formatNumber(total1.wiraswasta)}</td>
                            <td>${formatNumber(total1.pelajar)}</td>
                            <td>${formatNumber(total1.belumBekerja)}</td>
                            <td>${formatNumber(total1.lainnya)}</td>
                            <td class="bold">${formatNumber(total1.totalPekerjaan)}</td>
                        </tr>
                    </tbody>
                </table>

                <table style="width: 55%; margin-top: 14px;">
                    <thead>
                        <tr>
                            <th rowspan="3" style="width: 25px;">NO</th>
                            <th rowspan="3" style="min-width: 90px;">DUSUN</th>
                            <th colspan="10">KONDISI</th>
                        </tr>
                        <tr>
                            <th colspan="7">MENURUT AGAMA</th>
                            <th colspan="3">KEWARGANEGARAAN</th>
                        </tr>
                        <tr>
                            <th>ISLAM</th><th>KRISTEN</th><th>HINDU</th><th>BUDHA</th><th>KHONGHUCU</th><th>KEPERCAYAAN</th><th>JUMLAH</th>
                            <th>WNA</th><th>WNI</th><th>JUMLAH</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${rows2.joinToString("") { r ->
                            """
                            <tr>
                                <td>${r.no}</td>
                                <td class="text-left">${r.namaWilayah}</td>
                                <td>${if (r.islam > 0) formatNumber(r.islam) else "-"}</td>
                                <td>${if (r.kristen > 0) r.kristen else "-"}</td>
                                <td>${if (r.hindu > 0) r.hindu else "-"}</td>
                                <td>${if (r.budha > 0) r.budha else "-"}</td>
                                <td>${if (r.khonghucu > 0) r.khonghucu else "-"}</td>
                                <td>${if (r.kepercayaan > 0) r.kepercayaan else "-"}</td>
                                <td class="bold">${formatNumber(r.totalAgama)}</td>

                                <td>${if (r.wna > 0) r.wna else "-"}</td>
                                <td>${formatNumber(r.wni)}</td>
                                <td class="bold">${formatNumber(r.totalKewarganegaraan)}</td>
                            </tr>
                            """
                        }}
                        <tr class="bg-total">
                            <td colspan="2" class="bold">JUMLAH</td>
                            <td>${formatNumber(total2.islam)}</td>
                            <td>${if (total2.kristen > 0) total2.kristen else "-"}</td>
                            <td>${if (total2.hindu > 0) total2.hindu else "-"}</td>
                            <td>${if (total2.budha > 0) total2.budha else "-"}</td>
                            <td>${if (total2.khonghucu > 0) total2.khonghucu else "-"}</td>
                            <td>${if (total2.kepercayaan > 0) total2.kepercayaan else "-"}</td>
                            <td class="bold">${formatNumber(total2.totalAgama)}</td>

                            <td>${if (total2.wna > 0) total2.wna else "-"}</td>
                            <td>${formatNumber(total2.wni)}</td>
                            <td class="bold">${formatNumber(total2.totalKewarganegaraan)}</td>
                        </tr>
                    </tbody>
                </table>

                <div class="notes">
                    <b>KETERANGAN:</b><br>
                    - Kolom mata pencaharian pada jenis pekerjaan LAINNYA adalah jumlah keseluruhan jenis mata pencaharian yang tidak termasuk dalam kategori.<br>
                    - Jumlah penduduk berdasarkan umur jumlahnya harus sama dengan Jumlah penduduk berdasarkan pendidikan, mata pencaharian, agama, dan kewarganegaraan.<br>
                    - Jumlah penduduk berdasarkan lahir, mati, pindah datang, dan wajib KTP dan AKTE harus sama dengan Jumlah penduduk berdasarkan pendidikan, mata pencaharian, agama, dan kecuali jumlah KK (kartu keluarga).
                </div>
                """
            }
            3 -> {
                val (rows, total) = generateFormat3(allPenduduk, monthIndex1to12, year, wilayahTugasFilter)
                """
                <table>
                    <thead>
                        <tr>
                            <th rowspan="2" style="width: 25px;">NO</th>
                            <th rowspan="2" style="min-width: 90px;">DUSUN</th>
                            <th rowspan="2">LUAS WILAYAH<br>(Km Persegi)</th>
                            <th colspan="3">JUMLAH</th>
                            <th colspan="3">JUMLAH PENDUDUK<br>BLN LALU</th>
                            <th colspan="3">LAHIR BULAN INI</th>
                            <th colspan="3">MATI BULAN INI</th>
                            <th colspan="3">DATANG BLN INI</th>
                            <th colspan="3">PINDAH BLN INI</th>
                            <th colspan="3">JML PENDUDUK<br>BULAN INI</th>
                            <th colspan="3">WAJIB KTP</th>
                            <th colspan="3">KK</th>
                            <th colspan="3">AKTE</th>
                            <th colspan="3">KIA</th>
                        </tr>
                        <tr>
                            <th>RT</th><th>RW</th><th>DUSUN</th>
                            <th>LK</th><th>PR</th><th>JML</th>
                            <th>LK</th><th>PR</th><th>JML</th>
                            <th>LK</th><th>PR</th><th>JML</th>
                            <th>LK</th><th>PR</th><th>JML</th>
                            <th>LK</th><th>PR</th><th>JML</th>
                            <th>LK</th><th>PR</th><th>JML</th>
                            <th>TELAH<br>MEMILIKI<br>KTP</th><th>BELUM<br>MEMILIKI<br>KTP</th><th>JML</th>
                            <th>TELAH<br>MEMILIKI<br>KK</th><th>BELUM<br>MEMILIKI<br>KK</th><th>JML</th>
                            <th>TELAH<br>MEMILIKI<br>AKTA</th><th>BELUM<br>MEMILIKI<br>AKTA</th><th>JML</th>
                            <th>TELAH<br>MEMILIKI<br>KIA</th><th>BELUM<br>MEMILIKI<br>KIA</th><th>JML</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${rows.joinToString("") { r ->
                            """
                            <tr>
                                <td>${r.no}</td>
                                <td class="text-left">${r.namaWilayah}</td>
                                <td>${formatDecimal(r.luasWilayahKm)}</td>
                                <td>${r.jumlahRt}</td><td>${r.jumlahRw}</td><td>${r.jumlahDusun}</td>
                                <td>${formatNumber(r.blnLalu.male)}</td><td>${formatNumber(r.blnLalu.female)}</td><td class="bold">${formatNumber(r.blnLalu.total)}</td>
                                <td>${if (r.lahirBlnIni.male > 0) r.lahirBlnIni.male else "-"}</td><td>${if (r.lahirBlnIni.female > 0) r.lahirBlnIni.female else "-"}</td><td>${if (r.lahirBlnIni.total > 0) r.lahirBlnIni.total else "-"}</td>
                                <td>${if (r.matiBlnIni.male > 0) r.matiBlnIni.male else "-"}</td><td>${if (r.matiBlnIni.female > 0) r.matiBlnIni.female else "-"}</td><td>${if (r.matiBlnIni.total > 0) r.matiBlnIni.total else "-"}</td>
                                <td>${if (r.datangBlnIni.male > 0) r.datangBlnIni.male else "-"}</td><td>${if (r.datangBlnIni.female > 0) r.datangBlnIni.female else "-"}</td><td>${if (r.datangBlnIni.total > 0) r.datangBlnIni.total else "-"}</td>
                                <td>${if (r.pindahBlnIni.male > 0) r.pindahBlnIni.male else "-"}</td><td>${if (r.pindahBlnIni.female > 0) r.pindahBlnIni.female else "-"}</td><td>${if (r.pindahBlnIni.total > 0) r.pindahBlnIni.total else "-"}</td>
                                <td>${formatNumber(r.blnIni.male)}</td><td>${formatNumber(r.blnIni.female)}</td><td class="bold">${formatNumber(r.blnIni.total)}</td>
                                <td>${formatNumber(r.wajibKtpSudah)}</td><td>${r.wajibKtpBelum}</td><td class="bold">${formatNumber(r.totalWajibKtp)}</td>
                                <td>${formatNumber(r.kkSudah)}</td><td>${if (r.kkBelum > 0) r.kkBelum else "-"}</td><td class="bold">${formatNumber(r.totalKk)}</td>
                                <td>${formatNumber(r.akteSudah)}</td><td>${formatNumber(r.akteBelum)}</td><td class="bold">${formatNumber(r.totalAkte)}</td>
                                <td>${r.kiaSudah}</td><td>${r.kiaBelum}</td><td class="bold">${r.totalKia}</td>
                            </tr>
                            """
                        }}
                        <tr class="bg-total">
                            <td colspan="2" class="bold">JUMLAH</td>
                            <td>${formatDecimal(total.luasWilayahKm)}</td>
                            <td>${total.jumlahRt}</td><td>${total.jumlahRw}</td><td>${total.jumlahDusun}</td>
                            <td>${formatNumber(total.blnLalu.male)}</td><td>${formatNumber(total.blnLalu.female)}</td><td class="bold">${formatNumber(total.blnLalu.total)}</td>
                            <td>${if (total.lahirBlnIni.total > 0) total.lahirBlnIni.male else "-"}</td><td>${if (total.lahirBlnIni.total > 0) total.lahirBlnIni.female else "-"}</td><td>${if (total.lahirBlnIni.total > 0) total.lahirBlnIni.total else "-"}</td>
                            <td>${if (total.matiBlnIni.total > 0) total.matiBlnIni.male else "-"}</td><td>${if (total.matiBlnIni.total > 0) total.matiBlnIni.female else "-"}</td><td>${if (total.matiBlnIni.total > 0) total.matiBlnIni.total else "-"}</td>
                            <td>${if (total.datangBlnIni.total > 0) total.datangBlnIni.male else "-"}</td><td>${if (total.datangBlnIni.total > 0) total.datangBlnIni.female else "-"}</td><td>${if (total.datangBlnIni.total > 0) total.datangBlnIni.total else "-"}</td>
                            <td>${if (total.pindahBlnIni.total > 0) total.pindahBlnIni.male else "-"}</td><td>${if (total.pindahBlnIni.total > 0) total.pindahBlnIni.female else "-"}</td><td>${if (total.pindahBlnIni.total > 0) total.pindahBlnIni.total else "-"}</td>
                            <td>${formatNumber(total.blnIni.male)}</td><td>${formatNumber(total.blnIni.female)}</td><td class="bold">${formatNumber(total.blnIni.total)}</td>
                            <td>${formatNumber(total.wajibKtpSudah)}</td><td>${total.wajibKtpBelum}</td><td class="bold">${formatNumber(total.totalWajibKtp)}</td>
                            <td>${formatNumber(total.kkSudah)}</td><td>${if (total.kkBelum > 0) total.kkBelum else "-"}</td><td class="bold">${formatNumber(total.totalKk)}</td>
                            <td>${formatNumber(total.akteSudah)}</td><td>${formatNumber(total.akteBelum)}</td><td class="bold">${formatNumber(total.totalAkte)}</td>
                            <td>${total.kiaSudah}</td><td>${total.kiaBelum}</td><td class="bold">${total.totalKia}</td>
                        </tr>
                    </tbody>
                </table>
                """
            }
            else -> ""
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Laporan Data Kependudukan</title>
                $css
            </head>
            <body>
                $headerHtml
                $tableContent
                $signatureHtml
            </body>
            </html>
        """.trimIndent()
    }

    // ==========================================
    // NATIVE PRINTING HELPER (ANDROID PRINTMANAGER)
    // ==========================================
    private var activePrintWebView: WebView? = null

    fun printReport(
        context: Context,
        htmlContent: String,
        jobName: String = "Laporan_Kependudukan_Desa"
    ) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                Toast.makeText(context, "Layanan cetak tidak tersedia di perangkat ini", Toast.LENGTH_SHORT).show()
                return
            }

            val webView = WebView(context.applicationContext)
            activePrintWebView = webView

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    try {
                        val printAdapter = webView.createPrintDocumentAdapter(jobName)
                        val printAttributes = PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asLandscape())
                            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                            .build()
                        printManager.print(jobName, printAdapter, printAttributes)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Gagal memulai proses cetak: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                    // Return true to indicate the host application handled the renderer termination
                    // preventing application crash (code -1)
                    activePrintWebView = null
                    return true
                }
            }
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal memuat dokumen cetak: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ==========================================
    // SHARE TEXT / INTENT HELPER
    // ==========================================
    fun shareTextToApps(context: Context, text: String, subject: String = "Laporan Data Kependudukan") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Bagikan Laporan Kependudukan via:"))
    }
}
