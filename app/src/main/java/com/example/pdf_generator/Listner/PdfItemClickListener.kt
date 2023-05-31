package com.example.pdf_generator.Listner

import java.io.File

interface PdfItemClickListener {
    fun pdfItemClicked(file: File)
}