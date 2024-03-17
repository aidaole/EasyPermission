package com.aidaole.files

import com.aidaole.easypermission.ext.logi
import java.io.File

class FileScanner {
    companion object {
        private const val TAG = "FileScanner"
    }

    private val filterFiles = mutableListOf<File>()

    fun scanTxtFiles(directoryPath: String): List<File> {
        "scanTxtFiles-> $directoryPath".logi(TAG)
        scanFiles(directoryPath, ".txt")
        return filterFiles
    }

    private fun scanFiles(directoryPath: String, suffix: String) {
        val file = File(directoryPath)
        if (file.isFile) {
            if (file.name.endsWith(suffix)) {
                filterFiles.add(file)
            }
        } else if (file.isDirectory) {
            file.listFiles()?.forEach {
                scanFiles(it.absolutePath, suffix)
            }
        }
    }
}