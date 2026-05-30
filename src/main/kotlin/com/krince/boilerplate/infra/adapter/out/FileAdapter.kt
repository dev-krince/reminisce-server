package com.krince.boilerplate.infra.adapter.out

import com.krince.boilerplate.application.port.out.file.StoreFilePort
import com.krince.boilerplate.infra.config.properties.FileStorageProperties
import com.krince.boilerplate.shared.exception.BadRequestException
import com.krince.boilerplate.shared.response.ExceptionResponseCode.*
import com.krince.boilerplate.shared.util.FileUtil
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
@EnableConfigurationProperties(FileStorageProperties::class)
class FileAdapter(private val fileStorageProperties: FileStorageProperties) : StoreFilePort {
    override fun saveImage(file: MultipartFile?): String? = FileUtil.saveImage(file, fileStorageProperties.path)

    override fun saveImageOrThrows(file: MultipartFile?): String = FileUtil.saveImage(file, fileStorageProperties.path)
        ?: throw BadRequestException(INVALID_MULTIPART_FILE, INVALID_MULTIPART_FILE.message)

    override fun deleteFile(fileUrl: String?) = FileUtil.deleteFile(fileUrl, fileStorageProperties.path)
}