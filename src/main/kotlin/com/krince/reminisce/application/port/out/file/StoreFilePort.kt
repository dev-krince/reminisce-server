package com.krince.reminisce.application.port.out.file

import org.springframework.web.multipart.MultipartFile

interface StoreFilePort {
    fun saveImage(file: MultipartFile?): String?
    fun saveImageOrThrows(file: MultipartFile?): String
    fun deleteFile(fileUrl: String?)
}