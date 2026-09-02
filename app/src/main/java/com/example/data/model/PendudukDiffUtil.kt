package com.example.data.model

import org.json.JSONObject

data class FieldDiff(
    val fieldName: String,
    val oldValue: String,
    val newValue: String
)

object PendudukDiffUtil {
    fun getDiffList(old: Penduduk, new: Penduduk): List<FieldDiff> {
        val diffs = mutableListOf<FieldDiff>()
        fun check(field: String, oldVal: Any?, newVal: Any?) {
            val oldStr = oldVal?.toString()?.trim() ?: ""
            val newStr = newVal?.toString()?.trim() ?: ""
            if (oldStr != newStr) {
                diffs.add(FieldDiff(field, oldStr.ifBlank { "(Kosong)" }, newStr.ifBlank { "(Kosong)" }))
            }
        }

        check("Nomor Urut", old.no, new.no)
        check("Nama Lengkap", old.nama, new.nama)
        check("NIK", old.nik, new.nik)
        check("Jenis Kelamin", old.jenisKelamin, new.jenisKelamin)
        check("Tempat Lahir", old.tempatLahir, new.tempatLahir)
        check("Tanggal Lahir", old.tanggalLahir, new.tanggalLahir)
        check("Agama", old.agama, new.agama)
        check("Pendidikan Terakhir", old.pendidikanTerakhir, new.pendidikanTerakhir)
        check("Pekerjaan", old.pekerjaan, new.pekerjaan)
        check("Golongan Darah (GDR)", old.gdr, new.gdr)
        check("Status Perkawinan", old.statusPerkawinan, new.statusPerkawinan)
        check("Buku Nikah", old.bukuNikah, new.bukuNikah)
        check("SHDK", old.shdk, new.shdk)
        check("Kewarganegaraan", old.kewarganegaraan, new.kewarganegaraan)
        check("No. Paspor", old.noPaspor, new.noPaspor)
        check("No. KITAS", old.noKitas, new.noKitas)
        check("Nama Ayah", old.namaAyah, new.namaAyah)
        check("Nama Ibu", old.namaIbu, new.namaIbu)
        check("No. KK", old.noKk, new.noKk)
        check("Nama Kepala Keluarga", old.namaKepalaKeluarga, new.namaKepalaKeluarga)
        check("Alamat", old.alamat, new.alamat)
        check("RW", old.rw, new.rw)
        check("RT", old.rt, new.rt)
        check("Umur", old.umur, new.umur)
        check("Kepemilikan E-KTP", old.kepemilikanEKtp, new.kepemilikanEKtp)
        check("Tanggal Cetak KTP", old.tanggalPencetakan, new.tanggalPencetakan)
        check("Akta Kelahiran", old.kepemilikanAktaKelahiran, new.kepemilikanAktaKelahiran)
        check("Kartu KIA", old.kartuKia, new.kartuKia)
        check("Kartu PKH", old.kartuPkh, new.kartuPkh)
        check("Kartu BPNT", old.kartuBpnt, new.kartuBpnt)
        check("Kartu BPJS/KIS", old.kartuBpjsKis, new.kartuBpjsKis)
        check("Kartu KIP", old.kartuKip, new.kartuKip)
        check("Jenis KB", old.jenisKb, new.jenisKb)
        check("Usaha Yang Dijalankan", old.usahaYangDijalankan, new.usahaYangDijalankan)
        check("Listrik (Token/Pasca)", old.listrikJenis, new.listrikJenis)
        check("Kepemilikan Listrik", old.kepemilikanListrik, new.kepemilikanListrik)
        check("Daya Listrik", old.dayaListrik, new.dayaListrik)
        check("No Token / KWH", old.noTokenKwh, new.noTokenKwh)
        check("No Handphone", old.noHandphone, new.noHandphone)
        check("Anak Ke", old.anakKe, new.anakKe)
        check("Kepemilikan Rumah", old.kepemilikanRumah, new.kepemilikanRumah)
        check("Ukuran Rumah", old.ukuranRumah, new.ukuranRumah)
        check("Jenis Rumah", old.jenisRumah, new.jenisRumah)
        check("Keterangan", old.keterangan, new.keterangan)
        check("Vaksinasi", old.vaksinasi, new.vaksinasi)
        check("Disabilitas", old.disabilitas, new.disabilitas)
        check("Status Mutasi", old.statusMutasi, new.statusMutasi)
        check("Tanggal Kematian", old.tanggalKematian, new.tanggalKematian)
        check("Penyebab Kematian", old.penyebabKematian, new.penyebabKematian)
        check("Tempat Pemakaman", old.tempatPemakaman, new.tempatPemakaman)
        check("Tanggal Pindah", old.tanggalPindah, new.tanggalPindah)
        check("Alasan Pindah", old.alasanPindah, new.alasanPindah)
        check("Alamat Tujuan", old.alamatTujuan, new.alamatTujuan)
        check("Desa Tujuan", old.desaTujuan, new.desaTujuan)

        return diffs
    }

    fun toSnapshotJson(p: Penduduk): String {
        val json = JSONObject()
        json.put("NO", p.no)
        json.put("NAMA", p.nama)
        json.put("NIK", p.nik)
        json.put("JENIS KELAMIN", p.jenisKelamin)
        json.put("TEMPAT LAHIR", p.tempatLahir)
        json.put("TANGGAL LAHIR", p.tanggalLahir)
        json.put("AGAMA", p.agama)
        json.put("PENDIDIKAN TERAKHIR", p.pendidikanTerakhir)
        json.put("PEKERJAAN", p.pekerjaan)
        json.put("GDR", p.gdr)
        json.put("STATUS PERKAWINAN", p.statusPerkawinan)
        json.put("BUKU NIKAH", p.bukuNikah)
        json.put("SHDK", p.shdk)
        json.put("KEWARGANEGARAAN", p.kewarganegaraan)
        json.put("NO. PASPOR", p.noPaspor)
        json.put("NO KITAS", p.noKitas)
        json.put("NAMA AYAH", p.namaAyah)
        json.put("NAMA IBU", p.namaIbu)
        json.put("NO KK", p.noKk)
        json.put("NAMA KK", p.namaKepalaKeluarga)
        json.put("ALAMAT", p.alamat)
        json.put("RW", p.rw)
        json.put("RT", p.rt)
        json.put("UMUR", p.umur)
        json.put("KEPEMILIKAN E-KTP", p.kepemilikanEKtp)
        json.put("TANGGAL PENCETAKAN", p.tanggalPencetakan)
        json.put("KEPEMILIKAN AKTA KELAHIRAN", p.kepemilikanAktaKelahiran)
        json.put("KARTU KIA", p.kartuKia)
        json.put("KARTU PKH", p.kartuPkh)
        json.put("KARTU BPNT", p.kartuBpnt)
        json.put("KARTU BPJS/KIS", p.kartuBpjsKis)
        json.put("KARTU KIP", p.kartuKip)
        json.put("JENIS KB", p.jenisKb)
        json.put("USAHA YANG DIJALANKAN", p.usahaYangDijalankan)
        json.put("LISTRIK (TOKEN/ PASCA BAYAR)", p.listrikJenis)
        json.put("KEPEMILIKAN LISTRIK", p.kepemilikanListrik)
        json.put("DAYA LISTRIK", p.dayaListrik)
        json.put("NO TOKEN / KWH", p.noTokenKwh)
        json.put("NO HANDPHONE", p.noHandphone)
        json.put("ANAK KE", p.anakKe)
        json.put("KEPEMILIKAN RUMAH", p.kepemilikanRumah)
        json.put("UKURAN RUMAH", p.ukuranRumah)
        json.put("JENIS RUMAH", p.jenisRumah)
        json.put("KETERANGAN", p.keterangan)
        json.put("VAKSINASI", p.vaksinasi)
        json.put("DISABILITAS", p.disabilitas)
        json.put("STATUS_MUTASI", p.statusMutasi)
        json.put("TANGGAL_KEMATIAN", p.tanggalKematian)
        json.put("PENYEBAB_KEMATIAN", p.penyebabKematian)
        json.put("TANGGAL_PINDAH", p.tanggalPindah)
        json.put("ALASAN_PINDAH", p.alasanPindah)
        json.put("ALAMAT_TUJUAN", p.alamatTujuan)
        return json.toString()
    }

    fun parseDiffsFromJson(dataBefore: String?, dataAfter: String?): List<FieldDiff> {
        if (dataBefore.isNullOrBlank() || dataAfter.isNullOrBlank()) return emptyList()
        return try {
            val beforeJson = JSONObject(dataBefore)
            val afterJson = JSONObject(dataAfter)
            val diffs = mutableListOf<FieldDiff>()
            val keys = afterJson.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val valBefore = beforeJson.optString(key, "-")
                val valAfter = afterJson.optString(key, "-")
                if (valBefore != valAfter) {
                    diffs.add(FieldDiff(key, valBefore.ifBlank { "(Kosong)" }, valAfter.ifBlank { "(Kosong)" }))
                }
            }
            diffs
        } catch (e: Exception) {
            emptyList()
        }
    }
}
