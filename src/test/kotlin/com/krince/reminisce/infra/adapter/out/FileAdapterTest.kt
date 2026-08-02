package com.krince.reminisce.infra.adapter.out

import com.krince.reminisce.infra.config.properties.FileStorageProperties
import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_MULTIPART_FILE
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
import org.springframework.mock.web.MockMultipartFile
import java.nio.file.Files
import java.nio.file.Paths

@Tags("test", "unitTest")
@DisplayName("FileAdapter 단위테스트")
class FileAdapterTest : FunSpec({

    context("saveImage") {
        context("성공") {
            test("file이 null이면 null을 반환한다") {
                val storagePath = Files.createTempDirectory("fileadapter").toString()
                val props = FileStorageProperties(path = storagePath, cachePeriod = 3600)
                val adapter = FileAdapter(props)

                adapter.saveImage(null) shouldBe null

                Files.deleteIfExists(Paths.get(storagePath))
            }
            test("유효한 이미지 파일이면 저장 후 /files/ URL을 반환한다") {
                val storagePath = Files.createTempDirectory("fileadapter").toString()
                val props = FileStorageProperties(path = storagePath, cachePeriod = 3600)
                val adapter = FileAdapter(props)
                val file = MockMultipartFile(
                    "image",
                    "test.png",
                    "image/png",
                    "content".toByteArray()
                )

                val result = adapter.saveImage(file)

                result.shouldStartWith("/files/")
                result!!.shouldEndWith(".png")
                val fileName = result.removePrefix("/files/")
                Files.exists(Paths.get(storagePath, fileName)) shouldBe true
                Files.deleteIfExists(Paths.get(storagePath, fileName))
                Files.deleteIfExists(Paths.get(storagePath))
            }
        }
    }

    context("saveImageOrThrows") {
        context("성공") {
            test("유효한 이미지 파일이면 저장 후 URL을 반환한다") {
                val storagePath = Files.createTempDirectory("fileadapter").toString()
                val props = FileStorageProperties(path = storagePath, cachePeriod = 3600)
                val adapter = FileAdapter(props)
                val file = MockMultipartFile(
                    "image",
                    "thumb.png",
                    "image/png",
                    "content".toByteArray()
                )

                val result = adapter.saveImageOrThrows(file)

                result.shouldStartWith("/files/")
                result.shouldEndWith(".png")
                Files.deleteIfExists(Paths.get(storagePath, result.removePrefix("/files/")))
                Files.deleteIfExists(Paths.get(storagePath))
            }
        }
        context("실패") {
            test("file이 null이면 INVALID_MULTIPART_FILE 예외를 던진다") {
                val storagePath = Files.createTempDirectory("fileadapter").toString()
                val props = FileStorageProperties(path = storagePath, cachePeriod = 3600)
                val adapter = FileAdapter(props)

                val ex = shouldThrow<BadRequestException> { adapter.saveImageOrThrows(null) }

                ex.exceptionResponseCode shouldBe INVALID_MULTIPART_FILE
                Files.deleteIfExists(Paths.get(storagePath))
            }
        }
    }

    context("deleteFile") {
        context("성공") {
            test("fileUrl이 null이면 아무 동작도 하지 않는다") {
                val storagePath = Files.createTempDirectory("fileadapter").toString()
                val props = FileStorageProperties(path = storagePath, cachePeriod = 3600)
                val adapter = FileAdapter(props)

                adapter.deleteFile(null)

                Files.deleteIfExists(Paths.get(storagePath))
            }
            test("/files/ 로 시작하는 fileUrl이면 해당 파일을 삭제한다") {
                val storagePath = Files.createTempDirectory("fileadapter").toString()
                val fileName = "to-delete.png"
                val targetFile = Paths.get(storagePath, fileName)
                Files.write(targetFile, "content".toByteArray())
                val props = FileStorageProperties(path = storagePath, cachePeriod = 3600)
                val adapter = FileAdapter(props)

                adapter.deleteFile("/files/$fileName")

                Files.exists(targetFile) shouldBe false
                Files.deleteIfExists(Paths.get(storagePath))
            }
        }
    }
})
