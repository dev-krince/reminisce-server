package com.krince.boilerplate.shared.util

import com.krince.boilerplate.shared.exception.BadRequestException
import com.krince.boilerplate.shared.response.ExceptionResponseCode.INVALID_FILE_EXTENSION
import com.krince.boilerplate.shared.response.ExceptionResponseCode.REQUIRE_FILE_EXTENSION
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.string.shouldStartWith
import io.mockk.every
import io.mockk.mockk
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths

@Tags("test", "unitTest")
@DisplayName("FileUtil 단위테스트")
class FileUtilTest : FunSpec({

    context("saveImage") {
        context("성공") {
            test("file이 null이면 null을 반환한다") {
                val storagePath = Files.createTempDirectory("fileutil").toString()
                FileUtil.saveImage(null, storagePath) shouldBe null
                Files.deleteIfExists(Paths.get(storagePath))
            }
            test("유효한 이미지 확장자면 파일을 저장하고 generateFileNameFormat 기반 /files/ URL을 반환한다") {
                val storagePath = Files.createTempDirectory("fileutil").toString()
                val file = MockMultipartFile(
                    "image",
                    "test.png",
                    "image/png",
                    "image-content".toByteArray()
                )
                val result = FileUtil.saveImage(file, storagePath)
                result shouldStartWith "/files/"
                result!!.shouldEndWith(".png")
                val fileName = result.removePrefix("/files/")
                val nameWithoutExt = fileName.substringBeforeLast(".")
                nameWithoutExt.shouldMatch(Regex("^[0-9a-fA-F]{64}$"))
                Files.exists(Paths.get(storagePath, fileName)) shouldBe true
                Files.deleteIfExists(Paths.get(storagePath, fileName))
                Files.deleteIfExists(Paths.get(storagePath))
            }
        }
        context("실패") {
            test("originalFilename이 null이면 REQUIRE_FILE_EXTENSION 예외를 던진다") {
                val storagePath = Files.createTempDirectory("fileutil").toString()
                val file = mockk<MultipartFile>()
                every { file.originalFilename } returns null
                val ex = shouldThrow<BadRequestException> { FileUtil.saveImage(file, storagePath) }
                ex.exceptionResponseCode shouldBe REQUIRE_FILE_EXTENSION
                Files.deleteIfExists(Paths.get(storagePath))
            }
            test("originalFilename에 점이 없으면 확장자가 빈 문자열이 되어 INVALID_FILE_EXTENSION 예외를 던진다") {
                val storagePath = Files.createTempDirectory("fileutil").toString()
                val file = MockMultipartFile(
                    "image",
                    "noextension",
                    "application/octet-stream",
                    "content".toByteArray()
                )
                val ex = shouldThrow<BadRequestException> { FileUtil.saveImage(file, storagePath) }
                ex.exceptionResponseCode shouldBe INVALID_FILE_EXTENSION
                Files.deleteIfExists(Paths.get(storagePath))
            }
            test("허용되지 않은 확장자면 INVALID_FILE_EXTENSION 예외를 던진다") {
                val storagePath = Files.createTempDirectory("fileutil").toString()
                val file = MockMultipartFile(
                    "image",
                    "test.txt",
                    "text/plain",
                    "content".toByteArray()
                )
                val ex = shouldThrow<BadRequestException> { FileUtil.saveImage(file, storagePath) }
                ex.exceptionResponseCode shouldBe INVALID_FILE_EXTENSION
                Files.deleteIfExists(Paths.get(storagePath))
            }
        }
    }

    context("deleteFile") {
        context("성공") {
            test("fileUrl이 null이면 아무 동작도 하지 않는다") {
                val storagePath = Files.createTempDirectory("fileutil").toString()
                FileUtil.deleteFile(null, storagePath)
                Files.deleteIfExists(Paths.get(storagePath))
            }
            test("fileUrl이 /files/로 시작하지 않으면 삭제하지 않는다") {
                val storagePath = Files.createTempDirectory("fileutil").toString()
                val existingFile = Paths.get(storagePath, "keep.txt")
                Files.write(existingFile, "keep".toByteArray())
                FileUtil.deleteFile("/other/keep.txt", storagePath)
                Files.exists(existingFile) shouldBe true
                Files.deleteIfExists(existingFile)
                Files.deleteIfExists(Paths.get(storagePath))
            }
            test("/files/ 로 시작하는 유효한 fileUrl이면 해당 파일을 삭제한다") {
                val storagePath = Files.createTempDirectory("fileutil").toString()
                val fileName = "to-delete.png"
                val targetFile = Paths.get(storagePath, fileName)
                Files.write(targetFile, "content".toByteArray())
                FileUtil.deleteFile("/files/$fileName", storagePath)
                Files.exists(targetFile) shouldBe false
                Files.deleteIfExists(Paths.get(storagePath))
            }
        }
    }
})
