package com.krince.reminisce.shared.util

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object FileUtil {
    private const val FILES_PREFIX = "/files/"
    private val ALLOWED_IMAGE_EXTENSION = setOf("png", "jpg", "jpeg", "webp", "heic", "avif")

    fun saveImage(file: MultipartFile?, storageBasePath: String): String? =
        save(file, storageBasePath, ALLOWED_IMAGE_EXTENSION)

    fun deleteFile(fileUrl: String?, storageBashPath: String) = delete(fileUrl, storageBashPath)

    private fun save(
        file: MultipartFile?,
        storageBasePath: String,
        allowedExtensions: Set<String>,
    ): String? {
        file ?: return null

        val extension: String = (file.originalFilename
            ?: throw BadRequestException(REQUIRE_FILE_EXTENSION, REQUIRE_FILE_EXTENSION.message))
            .substringAfterLast(".", "").lowercase()

        require(extension in allowedExtensions) {
            throw BadRequestException(INVALID_FILE_EXTENSION, INVALID_FILE_EXTENSION.message)
        }

        val fileName = "${UuidGenerator.generateFileNameFormat()}.$extension"
        val targetPath: Path = Paths.get(storageBasePath, fileName)
        Files.createDirectories(targetPath.parent)
        file.transferTo(targetPath.toFile())

        return "$FILES_PREFIX$fileName"
    }

    private fun delete(fileUrl: String?, storageBasePath: String) {
        fileUrl ?: return
        if (fileUrl.startsWith(FILES_PREFIX).not()) return

        val fileName: String = fileUrl.removePrefix(FILES_PREFIX).trim()
        val targetPath: Path = Paths.get(storageBasePath, fileName).normalize()
        val basePath = Paths.get(storageBasePath).normalize()

        if (targetPath.startsWith(basePath).not()) return

        Files.deleteIfExists(targetPath)
    }
}