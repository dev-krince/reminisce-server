package com.krince.reminisce.infra.adapter.out

import com.krince.reminisce.application.port.out.file.StoreFilePort
import com.krince.reminisce.infra.config.properties.FileStorageProperties
import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_FILE_EXTENSION
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_MULTIPART_FILE
import com.krince.reminisce.shared.response.ExceptionResponseCode.REQUIRE_FILE_EXTENSION
import com.krince.reminisce.shared.util.FileUtil
import com.krince.reminisce.shared.util.UuidGenerator
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Component
@EnableConfigurationProperties(FileStorageProperties::class)
class FileAdapter(private val fileStorageProperties: FileStorageProperties) : StoreFilePort {

    companion object {
        private const val FILES_PREFIX = "/files/"
        private val ALLOWED_AUDIO_EXTENSION = setOf("m4a", "mp3", "wav", "webm", "aac", "ogg")
    }

    override fun saveImage(file: MultipartFile?): String? = FileUtil.saveImage(file, fileStorageProperties.path)

    override fun saveImageOrThrows(file: MultipartFile?): String = FileUtil.saveImage(file, fileStorageProperties.path)
        ?: throw BadRequestException(INVALID_MULTIPART_FILE, INVALID_MULTIPART_FILE.message)

    override fun saveAudioOrThrows(file: MultipartFile?): String =
        saveAudio(file ?: throw BadRequestException(INVALID_MULTIPART_FILE, INVALID_MULTIPART_FILE.message))

    override fun saveAudioBytes(bytes: ByteArray, extension: String): String {
        val normalized: String = extension.lowercase()
        if (normalized !in ALLOWED_AUDIO_EXTENSION) {
            throw BadRequestException(INVALID_FILE_EXTENSION, INVALID_FILE_EXTENSION.message)
        }
        val fileName = "${UuidGenerator.generateFileNameFormat()}.$normalized"
        val targetPath: Path = Paths.get(fileStorageProperties.path, fileName)
        Files.createDirectories(targetPath.parent)
        Files.write(targetPath, bytes)

        return "$FILES_PREFIX$fileName"
    }

    override fun deleteFile(fileUrl: String?) = FileUtil.deleteFile(fileUrl, fileStorageProperties.path)

    private fun saveAudio(file: MultipartFile): String {
        val extension: String = resolveAudioExtension(file)
        val fileName = "${UuidGenerator.generateFileNameFormat()}.$extension"
        val targetPath: Path = Paths.get(fileStorageProperties.path, fileName)
        Files.createDirectories(targetPath.parent)
        file.transferTo(targetPath.toFile())

        return "$FILES_PREFIX$fileName"
    }

    private fun resolveAudioExtension(file: MultipartFile): String {
        val extension: String = (file.originalFilename
            ?: throw BadRequestException(REQUIRE_FILE_EXTENSION, REQUIRE_FILE_EXTENSION.message))
            .substringAfterLast(".", "").lowercase()
        if (extension.isBlank()) {
            throw BadRequestException(REQUIRE_FILE_EXTENSION, REQUIRE_FILE_EXTENSION.message)
        }
        if (extension !in ALLOWED_AUDIO_EXTENSION) {
            throw BadRequestException(INVALID_FILE_EXTENSION, INVALID_FILE_EXTENSION.message)
        }

        return extension
    }
}
