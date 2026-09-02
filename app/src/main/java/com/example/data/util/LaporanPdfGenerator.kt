package com.example.data.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Penduduk
import com.example.data.model.UserProfile
import java.io.File
import java.io.FileOutputStream

object LaporanPdfGenerator {

    private const val PAGE_WIDTH = 842 // A4 Landscape width in points (72 dpi)
    private const val PAGE_HEIGHT = 595 // A4 Landscape height in points (72 dpi)
    private const val MARGIN_LEFT = 28f
    private const val MARGIN_TOP = 28f
    private const val MARGIN_RIGHT = 28f
    private const val MARGIN_BOTTOM = 28f

    // Color Palette for Official Documents
    private val COLOR_TEXT_PRIMARY = Color.rgb(20, 20, 20)
    private val COLOR_TEXT_MUTED = Color.rgb(90, 90, 90)
    private val COLOR_HEADER_BG = Color.rgb(232, 240, 248)
    private val COLOR_TOTAL_BG = Color.rgb(220, 230, 242)
    private val COLOR_BORDER = Color.rgb(130, 140, 155)
    private val COLOR_BORDER_LIGHT = Color.rgb(195, 205, 215)
    private val COLOR_ZEBRA = Color.rgb(249, 251, 254)

    /**
     * Generate PDF file and return the saved File instance
     */
    fun generatePdfFile(
        context: Context,
        formatType: Int,
        profile: UserProfile,
        monthIndex1to12: Int,
        year: Int,
        titimangsa: String,
        allPenduduk: List<Penduduk>,
        wilayahTugasFilter: String = "SEMUA"
    ): File {
        val pdfDoc = PdfDocument()
        val monthName = LaporanBulananGenerator.MONTH_NAMES.getOrNull(monthIndex1to12 - 1) ?: "JANUARI"
        val wilayahLabel = if (wilayahTugasFilter != "SEMUA" && wilayahTugasFilter.isNotBlank()) "DUSUN $wilayahTugasFilter" else "SEMUA DUSUN"

        when (formatType) {
            1 -> drawFormat1(pdfDoc, profile, monthName, year, titimangsa, allPenduduk, wilayahTugasFilter, wilayahLabel)
            2 -> drawFormat2(pdfDoc, profile, monthName, year, titimangsa, allPenduduk, wilayahTugasFilter, wilayahLabel)
            3 -> drawFormat3(pdfDoc, profile, monthName, year, titimangsa, allPenduduk, wilayahTugasFilter, wilayahLabel)
            else -> drawFormat1(pdfDoc, profile, monthName, year, titimangsa, allPenduduk, wilayahTugasFilter, wilayahLabel)
        }

        val reportsDir = File(context.cacheDir, "laporan_pdf")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }

        val safeDesaName = profile.namaDesa.replace(" ", "_").replace("[^a-zA-Z0-9_]".toRegex(), "")
        val safeWilayah = wilayahLabel.replace(" ", "_").replace("[^a-zA-Z0-9_]".toRegex(), "")
        val fileName = "Laporan_Bulanan_Format${formatType}_${safeDesaName}_${safeWilayah}_${monthName}_$year.pdf"
        val file = File(reportsDir, fileName)

        FileOutputStream(file).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()

        return file
    }

    /**
     * Share PDF directly via Android Sharesheet
     */
    fun sharePdf(
        context: Context,
        formatType: Int,
        profile: UserProfile,
        monthIndex1to12: Int,
        year: Int,
        titimangsa: String,
        allPenduduk: List<Penduduk>,
        wilayahTugasFilter: String = "SEMUA"
    ) {
        try {
            val file = generatePdfFile(
                context = context,
                formatType = formatType,
                profile = profile,
                monthIndex1to12 = monthIndex1to12,
                year = year,
                titimangsa = titimangsa,
                allPenduduk = allPenduduk,
                wilayahTugasFilter = wilayahTugasFilter
            )

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val monthName = LaporanBulananGenerator.MONTH_NAMES.getOrNull(monthIndex1to12 - 1) ?: "JANUARI"
            val formatTitle = when (formatType) {
                1 -> "Kelompok Umur"
                2 -> "Pendidikan & Pekerjaan"
                3 -> "Mutasi & Administrasi"
                else -> "Kependudukan"
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Laporan Bulanan Format $formatType ($formatTitle) - ${profile.namaDesa} $monthName $year")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Berikut terlampir dokumen resmi Laporan Bulanan Data Perkembangan Penduduk Format $formatType ($formatTitle) ${profile.namaDesa} periode $monthName $year dalam format PDF."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Bagikan Dokumen Laporan PDF:")
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membagikan PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Open generated PDF in device's default PDF viewer
     */
    fun openPdf(
        context: Context,
        formatType: Int,
        profile: UserProfile,
        monthIndex1to12: Int,
        year: Int,
        titimangsa: String,
        allPenduduk: List<Penduduk>,
        wilayahTugasFilter: String = "SEMUA"
    ) {
        try {
            val file = generatePdfFile(
                context = context,
                formatType = formatType,
                profile = profile,
                monthIndex1to12 = monthIndex1to12,
                year = year,
                titimangsa = titimangsa,
                allPenduduk = allPenduduk,
                wilayahTugasFilter = wilayahTugasFilter
            )

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(viewIntent, "Buka Dokumen PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membuka PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ==========================================
    // DRAW FORMAT 1: KELOMPOK UMUR
    // ==========================================
    private fun drawFormat1(
        pdfDoc: PdfDocument,
        profile: UserProfile,
        monthName: String,
        year: Int,
        titimangsa: String,
        allPenduduk: List<Penduduk>,
        wilayahTugasFilter: String,
        wilayahLabel: String
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var currentY = MARGIN_TOP

        // 1. Header & Title
        currentY = drawReportHeader(
            canvas = canvas,
            paint = paint,
            profile = profile,
            formatNumber = "FORMAT 1",
            title = "LAPORAN BULANAN DATA PERKEMBANGAN PENDUDUK MENURUT KELOMPOK UMUR",
            monthName = monthName,
            year = year,
            wilayahLabel = wilayahLabel,
            startY = currentY
        )

        // 2. Data calculation
        val (rows, totalRow) = LaporanBulananGenerator.generateFormat1(allPenduduk, wilayahTugasFilter)

        // Column widths definition for Format 1
        // Total usable width = 842 - 56 = 786 pt
        val colNoW = 20f
        val colWilayahW = 86f
        val colAgePairW = 38f // 19pt for male, 19pt for female
        val colTotalMaleW = 24f
        val colTotalFemaleW = 24f
        val colTotalGrandW = 28f

        val startX = MARGIN_LEFT
        val tableW = colNoW + colWilayahW + (16 * colAgePairW) + colTotalMaleW + colTotalFemaleW + colTotalGrandW

        // 3. Draw Table Header
        val headerH1 = 15f
        val headerH2 = 13f
        val totalHeaderH = headerH1 + headerH2

        // Header Background
        paint.style = Paint.Style.FILL
        paint.color = COLOR_HEADER_BG
        canvas.drawRect(startX, currentY, startX + tableW, currentY + totalHeaderH, paint)

        // Border Paint
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.6f
            color = COLOR_BORDER
        }

        // Header Text Paint
        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_PRIMARY
            textSize = 6.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val ageRanges = listOf(
            "0-5", "6-12", "13-15", "16-18", "19-24", "25-29",
            "30-34", "35-39", "40-44", "45-49", "50-54", "55-59",
            "60-64", "65-69", "70-74", "75+"
        )

        // Draw Row 1 Header Cells
        // NO
        drawCell(canvas, startX, currentY, colNoW, totalHeaderH, "NO", headerTextPaint, borderPaint)
        // WILAYAH
        val wilayahHeaderTitle = if (wilayahTugasFilter != "SEMUA" && wilayahTugasFilter.isNotBlank()) "RT / RW" else "DUSUN"
        drawCell(canvas, startX + colNoW, currentY, colWilayahW, totalHeaderH, wilayahHeaderTitle, headerTextPaint, borderPaint)

        // Age Group Categories
        var colX = startX + colNoW + colWilayahW
        for (range in ageRanges) {
            drawCell(canvas, colX, currentY, colAgePairW, headerH1, range, headerTextPaint, borderPaint)
            drawCell(canvas, colX, currentY + headerH1, colAgePairW / 2, headerH2, "LK", headerTextPaint, borderPaint)
            drawCell(canvas, colX + colAgePairW / 2, currentY + headerH1, colAgePairW / 2, headerH2, "PR", headerTextPaint, borderPaint)
            colX += colAgePairW
        }

        // Grand Total Header
        val totalGroupW = colTotalMaleW + colTotalFemaleW + colTotalGrandW
        drawCell(canvas, colX, currentY, totalGroupW, headerH1, "JUMLAH", headerTextPaint, borderPaint)
        drawCell(canvas, colX, currentY + headerH1, colTotalMaleW, headerH2, "LK", headerTextPaint, borderPaint)
        drawCell(canvas, colX + colTotalMaleW, currentY + headerH1, colTotalFemaleW, headerH2, "PR", headerTextPaint, borderPaint)
        drawCell(canvas, colX + colTotalMaleW + colTotalFemaleW, currentY + headerH1, colTotalGrandW, headerH2, "TOTAL", headerTextPaint, borderPaint)

        currentY += totalHeaderH

        // 4. Draw Data Rows
        val rowH = 15f
        val bodyTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_PRIMARY
            textSize = 6f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.CENTER
        }
        val leftAlignTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_PRIMARY
            textSize = 6f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.LEFT
        }

        rows.forEachIndexed { idx, row ->
            // Zebra striping
            if (idx % 2 == 1) {
                paint.style = Paint.Style.FILL
                paint.color = COLOR_ZEBRA
                canvas.drawRect(startX, currentY, startX + tableW, currentY + rowH, paint)
            }

            drawCell(canvas, startX, currentY, colNoW, rowH, "${row.no}", bodyTextPaint, borderPaint)
            drawCellTextLeft(canvas, startX + colNoW, currentY, colWilayahW, rowH, row.namaWilayah, leftAlignTextPaint, borderPaint, padLeft = 4f)

            var cellX = startX + colNoW + colWilayahW
            val counts = listOf(
                row.age0to5, row.age6to12, row.age13to15, row.age16to18, row.age19to24, row.age25to29,
                row.age30to34, row.age35to39, row.age40to44, row.age45to49, row.age50to54, row.age55to59,
                row.age60to64, row.age65to69, row.age70to74, row.age75Above
            )

            for (c in counts) {
                drawCell(canvas, cellX, currentY, colAgePairW / 2, rowH, if (c.male > 0) "${c.male}" else "-", bodyTextPaint, borderPaint)
                drawCell(canvas, cellX + colAgePairW / 2, currentY, colAgePairW / 2, rowH, if (c.female > 0) "${c.female}" else "-", bodyTextPaint, borderPaint)
                cellX += colAgePairW
            }

            // Totals
            drawCell(canvas, cellX, currentY, colTotalMaleW, rowH, "${row.totalMale}", bodyTextPaint, borderPaint)
            drawCell(canvas, cellX + colTotalMaleW, currentY, colTotalFemaleW, rowH, "${row.totalFemale}", bodyTextPaint, borderPaint)
            val grandTextPaint = Paint(bodyTextPaint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
            drawCell(canvas, cellX + colTotalMaleW + colTotalFemaleW, currentY, colTotalGrandW, rowH, "${row.grandTotal}", grandTextPaint, borderPaint)

            currentY += rowH
        }

        // 5. Total Row
        val totalRowH = 17f
        paint.style = Paint.Style.FILL
        paint.color = COLOR_TOTAL_BG
        canvas.drawRect(startX, currentY, startX + tableW, currentY + totalRowH, paint)

        val boldHeaderPaint = Paint(headerTextPaint).apply { textSize = 6.5f }
        drawCell(canvas, startX, currentY, colNoW + colWilayahW, totalRowH, totalRow.namaWilayah, boldHeaderPaint, borderPaint)

        var totalCellX = startX + colNoW + colWilayahW
        val totalCounts = listOf(
            totalRow.age0to5, totalRow.age6to12, totalRow.age13to15, totalRow.age16to18, totalRow.age19to24, totalRow.age25to29,
            totalRow.age30to34, totalRow.age35to39, totalRow.age40to44, totalRow.age45to49, totalRow.age50to54, totalRow.age55to59,
            totalRow.age60to64, totalRow.age65to69, totalRow.age70to74, totalRow.age75Above
        )
        for (c in totalCounts) {
            drawCell(canvas, totalCellX, currentY, colAgePairW / 2, totalRowH, "${c.male}", boldHeaderPaint, borderPaint)
            drawCell(canvas, totalCellX + colAgePairW / 2, currentY, colAgePairW / 2, totalRowH, "${c.female}", boldHeaderPaint, borderPaint)
            totalCellX += colAgePairW
        }
        drawCell(canvas, totalCellX, currentY, colTotalMaleW, totalRowH, "${totalRow.totalMale}", boldHeaderPaint, borderPaint)
        drawCell(canvas, totalCellX + colTotalMaleW, currentY, colTotalFemaleW, totalRowH, "${totalRow.totalFemale}", boldHeaderPaint, borderPaint)
        drawCell(canvas, totalCellX + colTotalMaleW + colTotalFemaleW, currentY, colTotalGrandW, totalRowH, "${totalRow.grandTotal}", boldHeaderPaint, borderPaint)

        currentY += totalRowH + 16f

        // 6. Signature Block
        drawSignatureBlock(canvas, profile, titimangsa, currentY)

        pdfDoc.finishPage(page)
    }

    // ==========================================
    // DRAW FORMAT 2: PENDIDIKAN, PEKERJAAN, AGAMA, WN
    // ==========================================
    private fun drawFormat2(
        pdfDoc: PdfDocument,
        profile: UserProfile,
        monthName: String,
        year: Int,
        titimangsa: String,
        allPenduduk: List<Penduduk>,
        wilayahTugasFilter: String,
        wilayahLabel: String
    ) {
        // Page 1: Bagian 1 (Tingkat Pendidikan & Mata Pencaharian)
        val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page1 = pdfDoc.startPage(pageInfo1)
        val canvas1 = page1.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var currentY = MARGIN_TOP

        // Header
        currentY = drawReportHeader(
            canvas = canvas1,
            paint = paint,
            profile = profile,
            formatNumber = "FORMAT 2 - BAGIAN I",
            title = "LAPORAN BULANAN TINGKAT PENDIDIKAN DAN MATA PENCAHARIAN",
            monthName = monthName,
            year = year,
            wilayahLabel = wilayahLabel,
            startY = currentY
        )

        val (rowsPendidikan, totalPendidikan, agamaData) = LaporanBulananGenerator.generateFormat2(allPenduduk, wilayahTugasFilter)
        val (rowsAgama, totalAgama) = agamaData

        val colNoW = 20f
        val colWilayahW = 86f
        val startX = MARGIN_LEFT

        // Table 1: Pendidikan (10 cols + Total) + Mata Pencaharian (10 cols + Total)
        val colPendW = 28f
        val colPendTotalW = 34f
        val colPekW = 28f
        val colPekTotalW = 34f

        val pendColsCount = 10
        val pekColsCount = 10
        val totalPendGroupW = (pendColsCount * colPendW) + colPendTotalW
        val totalPekGroupW = (pekColsCount * colPekW) + colPekTotalW
        val table1W = colNoW + colWilayahW + totalPendGroupW + totalPekGroupW

        val headerH1 = 14f
        val headerH2 = 12f
        val totalHeaderH = headerH1 + headerH2

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.6f
            color = COLOR_BORDER
        }

        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_PRIMARY
            textSize = 5.8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        // Header Background
        paint.style = Paint.Style.FILL
        paint.color = COLOR_HEADER_BG
        canvas1.drawRect(startX, currentY, startX + table1W, currentY + totalHeaderH, paint)

        // NO & WILAYAH
        drawCell(canvas1, startX, currentY, colNoW, totalHeaderH, "NO", headerTextPaint, borderPaint)
        val wilayahHeaderTitle = if (wilayahTugasFilter != "SEMUA" && wilayahTugasFilter.isNotBlank()) "RT / RW" else "DUSUN"
        drawCell(canvas1, startX + colNoW, currentY, colWilayahW, totalHeaderH, wilayahHeaderTitle, headerTextPaint, borderPaint)

        // Group 1: PENDIDIKAN
        var group1X = startX + colNoW + colWilayahW
        drawCell(canvas1, group1X, currentY, totalPendGroupW, headerH1, "TINGKAT PENDIDIKAN", headerTextPaint, borderPaint)
        val pendLabels = listOf("BLM SKL", "TDK SD", "SD", "SMP", "SMA", "D1/D2", "D3", "S1", "S2", "S3")
        pendLabels.forEachIndexed { i, lbl ->
            drawCell(canvas1, group1X + (i * colPendW), currentY + headerH1, colPendW, headerH2, lbl, headerTextPaint, borderPaint)
        }
        drawCell(canvas1, group1X + (pendColsCount * colPendW), currentY + headerH1, colPendTotalW, headerH2, "TOTAL", headerTextPaint, borderPaint)

        // Group 2: MATA PENCAHARIAN
        val group2X = group1X + totalPendGroupW
        drawCell(canvas1, group2X, currentY, totalPekGroupW, headerH1, "MATA PENCAHARIAN / PEKERJAAN", headerTextPaint, borderPaint)
        val pekLabels = listOf("PNS/TNI", "KARY", "BURUH", "TANI", "TERNAK", "NELAYAN", "WIRASWA", "PELAJAR", "BLM KJA", "LAIN")
        pekLabels.forEachIndexed { i, lbl ->
            drawCell(canvas1, group2X + (i * colPekW), currentY + headerH1, colPekW, headerH2, lbl, headerTextPaint, borderPaint)
        }
        drawCell(canvas1, group2X + (pekColsCount * colPekW), currentY + headerH1, colPekTotalW, headerH2, "TOTAL", headerTextPaint, borderPaint)

        currentY += totalHeaderH

        // Rows
        val rowH = 14f
        val bodyTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_PRIMARY
            textSize = 5.8f
            textAlign = Paint.Align.CENTER
        }
        val leftAlignTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_PRIMARY
            textSize = 5.8f
            textAlign = Paint.Align.LEFT
        }

        rowsPendidikan.forEachIndexed { idx, r ->
            if (idx % 2 == 1) {
                paint.style = Paint.Style.FILL
                paint.color = COLOR_ZEBRA
                canvas1.drawRect(startX, currentY, startX + table1W, currentY + rowH, paint)
            }
            drawCell(canvas1, startX, currentY, colNoW, rowH, "${r.no}", bodyTextPaint, borderPaint)
            drawCellTextLeft(canvas1, startX + colNoW, currentY, colWilayahW, rowH, r.namaWilayah, leftAlignTextPaint, borderPaint, 3f)

            var cellX = group1X
            val pendValues = listOf(r.belumSekolah, r.tidakTamatSd, r.tamatSd, r.tamatSmp, r.tamatSma, r.diploma12, r.diploma3, r.diploma4S1, r.strata2, r.strata3)
            pendValues.forEach { v ->
                drawCell(canvas1, cellX, currentY, colPendW, rowH, if (v > 0) "$v" else "-", bodyTextPaint, borderPaint)
                cellX += colPendW
            }
            val boldBodyPaint = Paint(bodyTextPaint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
            drawCell(canvas1, cellX, currentY, colPendTotalW, rowH, "${r.totalPendidikan}", boldBodyPaint, borderPaint)

            var pekX = group2X
            val pekValues = listOf(r.pnsTniPolri, r.karyawan, r.buruh, r.petani, r.peternak, r.nelayan, r.wiraswasta, r.pelajar, r.belumBekerja, r.lainnya)
            pekValues.forEach { v ->
                drawCell(canvas1, pekX, currentY, colPekW, rowH, if (v > 0) "$v" else "-", bodyTextPaint, borderPaint)
                pekX += colPekW
            }
            drawCell(canvas1, pekX, currentY, colPekTotalW, rowH, "${r.totalPekerjaan}", boldBodyPaint, borderPaint)

            currentY += rowH
        }

        // Total Row
        val totalRowH = 16f
        paint.style = Paint.Style.FILL
        paint.color = COLOR_TOTAL_BG
        canvas1.drawRect(startX, currentY, startX + table1W, currentY + totalRowH, paint)
        val boldHeaderPaint = Paint(headerTextPaint).apply { textSize = 6.2f }

        drawCell(canvas1, startX, currentY, colNoW + colWilayahW, totalRowH, totalPendidikan.namaWilayah, boldHeaderPaint, borderPaint)

        var t1CellX = group1X
        val totalPendValues = listOf(
            totalPendidikan.belumSekolah, totalPendidikan.tidakTamatSd, totalPendidikan.tamatSd, totalPendidikan.tamatSmp,
            totalPendidikan.tamatSma, totalPendidikan.diploma12, totalPendidikan.diploma3, totalPendidikan.diploma4S1,
            totalPendidikan.strata2, totalPendidikan.strata3
        )
        totalPendValues.forEach { v ->
            drawCell(canvas1, t1CellX, currentY, colPendW, totalRowH, "$v", boldHeaderPaint, borderPaint)
            t1CellX += colPendW
        }
        drawCell(canvas1, t1CellX, currentY, colPendTotalW, totalRowH, "${totalPendidikan.totalPendidikan}", boldHeaderPaint, borderPaint)

        var t2CellX = group2X
        val totalPekValues = listOf(
            totalPendidikan.pnsTniPolri, totalPendidikan.karyawan, totalPendidikan.buruh, totalPendidikan.petani,
            totalPendidikan.peternak, totalPendidikan.nelayan, totalPendidikan.wiraswasta, totalPendidikan.pelajar,
            totalPendidikan.belumBekerja, totalPendidikan.lainnya
        )
        totalPekValues.forEach { v ->
            drawCell(canvas1, t2CellX, currentY, colPekW, totalRowH, "$v", boldHeaderPaint, borderPaint)
            t2CellX += colPekW
        }
        drawCell(canvas1, t2CellX, currentY, colPekTotalW, totalRowH, "${totalPendidikan.totalPekerjaan}", boldHeaderPaint, borderPaint)

        currentY += totalRowH + 16f
        drawSignatureBlock(canvas1, profile, titimangsa, currentY)
        pdfDoc.finishPage(page1)

        // Page 2: Bagian 2 (Agama & Kewarganegaraan)
        val pageInfo2 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
        val page2 = pdfDoc.startPage(pageInfo2)
        val canvas2 = page2.canvas
        var currentY2 = MARGIN_TOP

        currentY2 = drawReportHeader(
            canvas = canvas2,
            paint = paint,
            profile = profile,
            formatNumber = "FORMAT 2 - BAGIAN II",
            title = "LAPORAN BULANAN DATA AGAMA DAN KEWARGANEGARAAN",
            monthName = monthName,
            year = year,
            wilayahLabel = wilayahLabel,
            startY = currentY2
        )

        // Table 2 columns
        val colAgamaW = 48f
        val colAgamaTotalW = 56f
        val colWnW = 50f
        val colWnTotalW = 56f

        val totalAgamaGroupW = (6 * colAgamaW) + colAgamaTotalW
        val totalWnGroupW = (2 * colWnW) + colWnTotalW
        val table2W = colNoW + colWilayahW + totalAgamaGroupW + totalWnGroupW

        // Header Background
        paint.style = Paint.Style.FILL
        paint.color = COLOR_HEADER_BG
        canvas2.drawRect(startX, currentY2, startX + table2W, currentY2 + totalHeaderH, paint)

        drawCell(canvas2, startX, currentY2, colNoW, totalHeaderH, "NO", headerTextPaint, borderPaint)
        drawCell(canvas2, startX + colNoW, currentY2, colWilayahW, totalHeaderH, wilayahHeaderTitle, headerTextPaint, borderPaint)

        // Group 1: AGAMA
        val agmX = startX + colNoW + colWilayahW
        drawCell(canvas2, agmX, currentY2, totalAgamaGroupW, headerH1, "PEMELUK AGAMA", headerTextPaint, borderPaint)
        val agamaLabels = listOf("ISLAM", "KRISTEN", "HINDU", "BUDHA", "KHONGHUCU", "KEPERCAYAAN")
        agamaLabels.forEachIndexed { i, lbl ->
            drawCell(canvas2, agmX + (i * colAgamaW), currentY2 + headerH1, colAgamaW, headerH2, lbl, headerTextPaint, borderPaint)
        }
        drawCell(canvas2, agmX + (6 * colAgamaW), currentY2 + headerH1, colAgamaTotalW, headerH2, "TOTAL AGAMA", headerTextPaint, borderPaint)

        // Group 2: KEWARGANEGARAAN
        val wnX = agmX + totalAgamaGroupW
        drawCell(canvas2, wnX, currentY2, totalWnGroupW, headerH1, "KEWARGANEGARAAN", headerTextPaint, borderPaint)
        drawCell(canvas2, wnX, currentY2 + headerH1, colWnW, headerH2, "WNI", headerTextPaint, borderPaint)
        drawCell(canvas2, wnX + colWnW, currentY2 + headerH1, colWnW, headerH2, "WNA", headerTextPaint, borderPaint)
        drawCell(canvas2, wnX + (2 * colWnW), currentY2 + headerH1, colWnTotalW, headerH2, "TOTAL WN", headerTextPaint, borderPaint)

        currentY2 += totalHeaderH

        rowsAgama.forEachIndexed { idx, r ->
            if (idx % 2 == 1) {
                paint.style = Paint.Style.FILL
                paint.color = COLOR_ZEBRA
                canvas2.drawRect(startX, currentY2, startX + table2W, currentY2 + rowH, paint)
            }
            drawCell(canvas2, startX, currentY2, colNoW, rowH, "${r.no}", bodyTextPaint, borderPaint)
            drawCellTextLeft(canvas2, startX + colNoW, currentY2, colWilayahW, rowH, r.namaWilayah, leftAlignTextPaint, borderPaint, 4f)

            var agCellX = agmX
            val agValues = listOf(r.islam, r.kristen, r.hindu, r.budha, r.khonghucu, r.kepercayaan)
            agValues.forEach { v ->
                drawCell(canvas2, agCellX, currentY2, colAgamaW, rowH, if (v > 0) "$v" else "-", bodyTextPaint, borderPaint)
                agCellX += colAgamaW
            }
            val boldBodyPaint = Paint(bodyTextPaint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
            drawCell(canvas2, agCellX, currentY2, colAgamaTotalW, rowH, "${r.totalAgama}", boldBodyPaint, borderPaint)

            drawCell(canvas2, wnX, currentY2, colWnW, rowH, "${r.wni}", bodyTextPaint, borderPaint)
            drawCell(canvas2, wnX + colWnW, currentY2, colWnW, rowH, if (r.wna > 0) "${r.wna}" else "-", bodyTextPaint, borderPaint)
            drawCell(canvas2, wnX + (2 * colWnW), currentY2, colWnTotalW, rowH, "${r.totalKewarganegaraan}", boldBodyPaint, borderPaint)

            currentY2 += rowH
        }

        // Total
        paint.style = Paint.Style.FILL
        paint.color = COLOR_TOTAL_BG
        canvas2.drawRect(startX, currentY2, startX + table2W, currentY2 + totalRowH, paint)

        drawCell(canvas2, startX, currentY2, colNoW + colWilayahW, totalRowH, totalAgama.namaWilayah, boldHeaderPaint, borderPaint)

        var tAgCellX = agmX
        val tAgValues = listOf(totalAgama.islam, totalAgama.kristen, totalAgama.hindu, totalAgama.budha, totalAgama.khonghucu, totalAgama.kepercayaan)
        tAgValues.forEach { v ->
            drawCell(canvas2, tAgCellX, currentY2, colAgamaW, totalRowH, "$v", boldHeaderPaint, borderPaint)
            tAgCellX += colAgamaW
        }
        drawCell(canvas2, tAgCellX, currentY2, colAgamaTotalW, totalRowH, "${totalAgama.totalAgama}", boldHeaderPaint, borderPaint)

        drawCell(canvas2, wnX, currentY2, colWnW, totalRowH, "${totalAgama.wni}", boldHeaderPaint, borderPaint)
        drawCell(canvas2, wnX + colWnW, currentY2, colWnW, totalRowH, if (totalAgama.wna > 0) "${totalAgama.wna}" else "-", boldHeaderPaint, borderPaint)
        drawCell(canvas2, wnX + (2 * colWnW), currentY2, colWnTotalW, totalRowH, "${totalAgama.totalKewarganegaraan}", boldHeaderPaint, borderPaint)

        currentY2 += totalRowH + 16f
        drawSignatureBlock(canvas2, profile, titimangsa, currentY2)
        pdfDoc.finishPage(page2)
    }

    // ==========================================
    // DRAW FORMAT 3: MUTASI & ADMINISTRASI
    // ==========================================
    private fun drawFormat3(
        pdfDoc: PdfDocument,
        profile: UserProfile,
        monthName: String,
        year: Int,
        titimangsa: String,
        allPenduduk: List<Penduduk>,
        wilayahTugasFilter: String,
        wilayahLabel: String
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var currentY = MARGIN_TOP

        // Header
        currentY = drawReportHeader(
            canvas = canvas,
            paint = paint,
            profile = profile,
            formatNumber = "FORMAT 3",
            title = "LAPORAN BULANAN MUTASI PENDUDUK & KELENGKAPAN ADMINISTRASI",
            monthName = monthName,
            year = year,
            wilayahLabel = wilayahLabel,
            startY = currentY
        )

        val monthIndex = (LaporanBulananGenerator.MONTH_NAMES.indexOf(monthName) + 1).coerceIn(1, 12)
        val (rows, totalRow) = LaporanBulananGenerator.generateFormat3(allPenduduk, monthIndex, year, wilayahTugasFilter)

        val colNoW = 18f
        val colWilayahW = 76f
        val colLuasW = 32f
        val colAdminW = 15f // RT, RW, DUSUN @ 15pt = 45pt
        val startX = MARGIN_LEFT

        // Mutasi categories: Bln Lalu, Lahir, Mati, Datang, Pindah, Bln Ini (6 categories)
        // Each category has LK, PR, JML -> 3 x 14pt = 42pt per category x 6 = 252pt
        val colMutSubW = 13.5f
        val colMutCatW = colMutSubW * 3

        // Dokumen categories: Wajib KTP, KK, Akte, KIA (4 categories)
        // Each category has SUDAH, BELUM, TOTAL -> 3 x 14pt = 42pt x 4 = 168pt
        val colDocSubW = 14f
        val colDocCatW = colDocSubW * 3

        val totalTableW = colNoW + colWilayahW + colLuasW + (3 * colAdminW) + (6 * colMutCatW) + (4 * colDocCatW)

        val headerH1 = 14f
        val headerH2 = 12f
        val totalHeaderH = headerH1 + headerH2

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.6f
            color = COLOR_BORDER
        }

        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_PRIMARY
            textSize = 5.2f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        // Header Background
        paint.style = Paint.Style.FILL
        paint.color = COLOR_HEADER_BG
        canvas.drawRect(startX, currentY, startX + totalTableW, currentY + totalHeaderH, paint)

        // Headers
        drawCell(canvas, startX, currentY, colNoW, totalHeaderH, "NO", headerTextPaint, borderPaint)
        val wilayahHeaderTitle = if (wilayahTugasFilter != "SEMUA" && wilayahTugasFilter.isNotBlank()) "RT / RW" else "DUSUN"
        drawCell(canvas, startX + colNoW, currentY, colWilayahW, totalHeaderH, wilayahHeaderTitle, headerTextPaint, borderPaint)
        drawCell(canvas, startX + colNoW + colWilayahW, currentY, colLuasW, totalHeaderH, "LUAS\n(Km²)", headerTextPaint, borderPaint)

        val adminX = startX + colNoW + colWilayahW + colLuasW
        drawCell(canvas, adminX, currentY, 3 * colAdminW, headerH1, "WILAYAH", headerTextPaint, borderPaint)
        drawCell(canvas, adminX, currentY + headerH1, colAdminW, headerH2, "RT", headerTextPaint, borderPaint)
        drawCell(canvas, adminX + colAdminW, currentY + headerH1, colAdminW, headerH2, "RW", headerTextPaint, borderPaint)
        drawCell(canvas, adminX + (2 * colAdminW), currentY + headerH1, colAdminW, headerH2, "DSN", headerTextPaint, borderPaint)

        // Mutasi Headers
        var mutX = adminX + (3 * colAdminW)
        val mutTitles = listOf("BLN LALU", "LAHIR", "MATI", "DATANG", "PINDAH", "BLN INI")
        mutTitles.forEach { title ->
            drawCell(canvas, mutX, currentY, colMutCatW, headerH1, title, headerTextPaint, borderPaint)
            drawCell(canvas, mutX, currentY + headerH1, colMutSubW, headerH2, "LK", headerTextPaint, borderPaint)
            drawCell(canvas, mutX + colMutSubW, currentY + headerH1, colMutSubW, headerH2, "PR", headerTextPaint, borderPaint)
            drawCell(canvas, mutX + (2 * colMutSubW), currentY + headerH1, colMutSubW, headerH2, "JML", headerTextPaint, borderPaint)
            mutX += colMutCatW
        }

        // Dokumen Headers
        var docX = mutX
        val docTitles = listOf("WAJIB KTP", "KARTU KLG (KK)", "AKTE LAHIR", "KARTU KIA")
        docTitles.forEach { title ->
            drawCell(canvas, docX, currentY, colDocCatW, headerH1, title, headerTextPaint, borderPaint)
            drawCell(canvas, docX, currentY + headerH1, colDocSubW, headerH2, "SDH", headerTextPaint, borderPaint)
            drawCell(canvas, docX + colDocSubW, currentY + headerH1, colDocSubW, headerH2, "BLM", headerTextPaint, borderPaint)
            drawCell(canvas, docX + (2 * colDocSubW), currentY + headerH1, colDocSubW, headerH2, "JML", headerTextPaint, borderPaint)
            docX += colDocCatW
        }

        currentY += totalHeaderH

        // Body rows
        val rowH = 14f
        val bodyTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_PRIMARY
            textSize = 5.2f
            textAlign = Paint.Align.CENTER
        }
        val leftAlignTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_PRIMARY
            textSize = 5.2f
            textAlign = Paint.Align.LEFT
        }

        rows.forEachIndexed { idx, r ->
            if (idx % 2 == 1) {
                paint.style = Paint.Style.FILL
                paint.color = COLOR_ZEBRA
                canvas.drawRect(startX, currentY, startX + totalTableW, currentY + rowH, paint)
            }

            drawCell(canvas, startX, currentY, colNoW, rowH, "${r.no}", bodyTextPaint, borderPaint)
            drawCellTextLeft(canvas, startX + colNoW, currentY, colWilayahW, rowH, r.namaWilayah, leftAlignTextPaint, borderPaint, 3f)
            drawCell(canvas, startX + colNoW + colWilayahW, currentY, colLuasW, rowH, LaporanBulananGenerator.formatDecimal(r.luasWilayahKm), bodyTextPaint, borderPaint)

            drawCell(canvas, adminX, currentY, colAdminW, rowH, "${r.jumlahRt}", bodyTextPaint, borderPaint)
            drawCell(canvas, adminX + colAdminW, currentY, colAdminW, rowH, "${r.jumlahRw}", bodyTextPaint, borderPaint)
            drawCell(canvas, adminX + (2 * colAdminW), currentY, colAdminW, rowH, "${r.jumlahDusun}", bodyTextPaint, borderPaint)

            // Mutasi values
            var mX = adminX + (3 * colAdminW)
            val mutLists = listOf(r.blnLalu, r.lahirBlnIni, r.matiBlnIni, r.datangBlnIni, r.pindahBlnIni, r.blnIni)
            mutLists.forEach { gc ->
                drawCell(canvas, mX, currentY, colMutSubW, rowH, if (gc.male > 0) "${gc.male}" else "-", bodyTextPaint, borderPaint)
                drawCell(canvas, mX + colMutSubW, currentY, colMutSubW, rowH, if (gc.female > 0) "${gc.female}" else "-", bodyTextPaint, borderPaint)
                val boldBody = Paint(bodyTextPaint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
                drawCell(canvas, mX + (2 * colMutSubW), currentY, colMutSubW, rowH, if (gc.total > 0) "${gc.total}" else "-", boldBody, borderPaint)
                mX += colMutCatW
            }

            // Dokumen values
            var dX = mX
            val docLists = listOf(
                Triple(r.wajibKtpSudah, r.wajibKtpBelum, r.totalWajibKtp),
                Triple(r.kkSudah, r.kkBelum, r.totalKk),
                Triple(r.akteSudah, r.akteBelum, r.totalAkte),
                Triple(r.kiaSudah, r.kiaBelum, r.totalKia)
            )
            docLists.forEach { (sdh, blm, total) ->
                drawCell(canvas, dX, currentY, colDocSubW, rowH, if (sdh > 0) "$sdh" else "-", bodyTextPaint, borderPaint)
                drawCell(canvas, dX + colDocSubW, currentY, colDocSubW, rowH, if (blm > 0) "$blm" else "-", bodyTextPaint, borderPaint)
                val boldBody = Paint(bodyTextPaint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
                drawCell(canvas, dX + (2 * colDocSubW), currentY, colDocSubW, rowH, if (total > 0) "$total" else "-", boldBody, borderPaint)
                dX += colDocCatW
            }

            currentY += rowH
        }

        // Total Row
        val totalRowH = 16f
        paint.style = Paint.Style.FILL
        paint.color = COLOR_TOTAL_BG
        canvas.drawRect(startX, currentY, startX + totalTableW, currentY + totalRowH, paint)

        val boldHeaderPaint = Paint(headerTextPaint).apply { textSize = 5.6f }
        drawCell(canvas, startX, currentY, colNoW + colWilayahW, totalRowH, totalRow.namaWilayah, boldHeaderPaint, borderPaint)
        drawCell(canvas, startX + colNoW + colWilayahW, currentY, colLuasW, totalRowH, LaporanBulananGenerator.formatDecimal(totalRow.luasWilayahKm), boldHeaderPaint, borderPaint)

        drawCell(canvas, adminX, currentY, colAdminW, totalRowH, "${totalRow.jumlahRt}", boldHeaderPaint, borderPaint)
        drawCell(canvas, adminX + colAdminW, currentY, colAdminW, totalRowH, "${totalRow.jumlahRw}", boldHeaderPaint, borderPaint)
        drawCell(canvas, adminX + (2 * colAdminW), currentY, colAdminW, totalRowH, "${totalRow.jumlahDusun}", boldHeaderPaint, borderPaint)

        var tMX = adminX + (3 * colAdminW)
        val tMutLists = listOf(totalRow.blnLalu, totalRow.lahirBlnIni, totalRow.matiBlnIni, totalRow.datangBlnIni, totalRow.pindahBlnIni, totalRow.blnIni)
        tMutLists.forEach { gc ->
            drawCell(canvas, tMX, currentY, colMutSubW, totalRowH, "${gc.male}", boldHeaderPaint, borderPaint)
            drawCell(canvas, tMX + colMutSubW, currentY, colMutSubW, totalRowH, "${gc.female}", boldHeaderPaint, borderPaint)
            drawCell(canvas, tMX + (2 * colMutSubW), currentY, colMutSubW, totalRowH, "${gc.total}", boldHeaderPaint, borderPaint)
            tMX += colMutCatW
        }

        var tDX = tMX
        val tDocLists = listOf(
            Triple(totalRow.wajibKtpSudah, totalRow.wajibKtpBelum, totalRow.totalWajibKtp),
            Triple(totalRow.kkSudah, totalRow.kkBelum, totalRow.totalKk),
            Triple(totalRow.akteSudah, totalRow.akteBelum, totalRow.totalAkte),
            Triple(totalRow.kiaSudah, totalRow.kiaBelum, totalRow.totalKia)
        )
        tDocLists.forEach { (sdh, blm, total) ->
            drawCell(canvas, tDX, currentY, colDocSubW, totalRowH, "$sdh", boldHeaderPaint, borderPaint)
            drawCell(canvas, tDX + colDocSubW, currentY, colDocSubW, totalRowH, if (blm > 0) "$blm" else "-", boldHeaderPaint, borderPaint)
            drawCell(canvas, tDX + (2 * colDocSubW), currentY, colDocSubW, totalRowH, "$total", boldHeaderPaint, borderPaint)
            tDX += colDocCatW
        }

        currentY += totalRowH + 16f
        drawSignatureBlock(canvas, profile, titimangsa, currentY)
        pdfDoc.finishPage(page)
    }

    // ==========================================
    // DRAW HELPER: HEADER & KOP SURAT
    // ==========================================
    private fun drawReportHeader(
        canvas: Canvas,
        paint: Paint,
        profile: UserProfile,
        formatNumber: String,
        title: String,
        monthName: String,
        year: Int,
        wilayahLabel: String,
        startY: Float
    ): Float {
        var y = startY
        val centerX = PAGE_WIDTH / 2f

        // Top line header
        paint.color = COLOR_TEXT_PRIMARY
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val kabText = "PEMERINTAH KABUPATEN ${profile.kabupaten.uppercase()}"
        val kecText = "KECAMATAN ${profile.kecamatan.uppercase()} - DESA ${profile.namaDesa.uppercase()}"

        canvas.drawText(kabText, centerX, y, paint)
        y += 11f
        canvas.drawText(kecText, centerX, y, paint)
        y += 12f

        // Format Badge & Title
        paint.textSize = 10.5f
        paint.color = Color.rgb(0, 50, 110)
        canvas.drawText("$formatNumber : $title", centerX, y, paint)
        y += 11f

        // Subtitle / Periode & Wilayah
        paint.textSize = 7.5f
        paint.typeface = Typeface.DEFAULT
        paint.color = COLOR_TEXT_MUTED
        val subtitle = "BULAN: $monthName $year   |   WILAYAH: $wilayahLabel   |   KODE POS: ${profile.kodePos.ifBlank { "46471" }}"
        canvas.drawText(subtitle, centerX, y, paint)
        y += 8f

        // Double Divider Line
        paint.color = COLOR_BORDER
        paint.strokeWidth = 1.2f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y, paint)
        y += 2f
        paint.strokeWidth = 0.5f
        canvas.drawLine(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y, paint)
        y += 8f

        return y
    }

    // ==========================================
    // DRAW HELPER: SIGNATURE BLOCK
    // ==========================================
    private fun drawSignatureBlock(
        canvas: Canvas,
        profile: UserProfile,
        titimangsa: String,
        startY: Float
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_PRIMARY
            textSize = 7.5f
            typeface = Typeface.DEFAULT
        }

        val leftX = MARGIN_LEFT + 90f
        val rightX = PAGE_WIDTH - MARGIN_RIGHT - 150f

        var y = startY
        // Left Column: Kepala Desa
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Mengetahui,", leftX, y, paint)
        y += 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("KEPALA DESA ${profile.namaDesa.uppercase()}", leftX, y, paint)

        // Right Column: Kasi Pemerintahan
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(titimangsa, rightX, startY, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("KASI PEMERINTAHAN", rightX, startY + 9.5f, paint)

        val signatureSpace = 38f
        val signBottomY = y + signatureSpace

        // Left Name
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(profile.namaKades.ifBlank { "KEPALA DESA" }, leftX, signBottomY, paint)
        paint.strokeWidth = 0.6f
        val kadesTextW = paint.measureText(profile.namaKades.ifBlank { "KEPALA DESA" })
        canvas.drawLine(leftX - (kadesTextW / 2), signBottomY + 1.5f, leftX + (kadesTextW / 2), signBottomY + 1.5f, paint)

        // Right Name
        canvas.drawText(profile.namaPetugas.ifBlank { "PETUGAS REGISTRASI" }, rightX, signBottomY, paint)
        val petugasTextW = paint.measureText(profile.namaPetugas.ifBlank { "PETUGAS REGISTRASI" })
        canvas.drawLine(rightX - (petugasTextW / 2), signBottomY + 1.5f, rightX + (petugasTextW / 2), signBottomY + 1.5f, paint)

        // NIP
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 6.5f
        val nipText = if (profile.nipPetugas.isNotBlank()) "NIP: ${profile.nipPetugas}" else "NIP: -"
        canvas.drawText(nipText, rightX, signBottomY + 9f, paint)
    }

    // ==========================================
    // DRAW HELPER: CELL UTILS
    // ==========================================
    private fun drawCell(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        text: String,
        textPaint: Paint,
        borderPaint: Paint
    ) {
        canvas.drawRect(x, y, x + width, y + height, borderPaint)
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val textX = x + (width / 2f)
        val textY = y + (height / 2f) + (textBounds.height() / 2f) - 1f
        canvas.drawText(text, textX, textY, textPaint)
    }

    private fun drawCellTextLeft(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        text: String,
        textPaint: Paint,
        borderPaint: Paint,
        padLeft: Float = 4f
    ) {
        canvas.drawRect(x, y, x + width, y + height, borderPaint)
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val textX = x + padLeft
        val textY = y + (height / 2f) + (textBounds.height() / 2f) - 1f

        // Truncate if text exceeds cell width
        var displayText = text
        val maxTextW = width - (padLeft * 2)
        if (textPaint.measureText(displayText) > maxTextW) {
            while (displayText.isNotEmpty() && textPaint.measureText("$displayText…") > maxTextW) {
                displayText = displayText.dropLast(1)
            }
            displayText = "$displayText…"
        }

        canvas.drawText(displayText, textX, textY, textPaint)
    }
}
