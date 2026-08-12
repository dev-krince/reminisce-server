package com.krince.reminisce.infra.adapter.out

import com.krince.reminisce.infra.config.properties.FileStorageProperties
import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_FILE_EXTENSION
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_MULTIPART_FILE
import com.krince.reminisce.shared.response.ExceptionResponseCode.REQUIRE_FILE_EXTENSION
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

    context("saveAudioOrThrows") {
        context("성공") {
            test("유효한 오디오 파일이면 저장 후 /files/ URL을 반환하고 실제 파일이 생성된다") {
                val storagePath = Files.createTempDirectory("fileadapter").toString()
                val props = FileStorageProperties(path = storagePath, cachePeriod = 3600)
                val adapter = FileAdapter(props)
                val file = MockMultipartFile(
                    "audio",
                    "retelling.m4a",
                    "audio/mp4",
                    "audio-content".toByteArray()
                )

                val result = adapter.saveAudioOrThrows(file)

                result.shouldStartWith("/files/")
                result.shouldEndWith(".m4a")
                val fileName = result.removePrefix("/files/")
                Files.exists(Paths.get(storagePath, fileName)) shouldBe true
                Files.deleteIfExists(Paths.get(storagePath, fileName))
                Files.deleteIfExists(Paths.get(storagePath))
            }
        }
        context("실패") {
            test("file이 null이면 INVALID_MULTIPART_FILE 예외를 던진다") {
                val storagePath = Files.createTempDirectory("fileadapter").toString()
                val props = FileStorageProperties(path = storagePath, cachePeriod = 3600)
                val adapter = FileAdapter(props)

                val ex = shouldThrow<BadRequestException> { adapter.saveAudioOrThrows(null) }

                ex.exceptionResponseCode shouldBe INVALID_MULTIPART_FILE
                Files.deleteIfExists(Paths.get(storagePath))
            }

            test("확장자가 없으면 REQUIRE_FILE_EXTENSION 예외를 던진다") {
                val storagePath = Files.createTempDirectory("fileadapter").toString()
                val props = FileStorageProperties(path = storagePath, cachePeriod = 3600)
                val adapter = FileAdapter(props)
                val file = MockMultipartFile(
                    "audio",
                    "retelling",
                    "audio/mp4",
                    "audio-content".toByteArray()
                )

                val ex = shouldThrow<BadRequestException> { adapter.saveAudioOrThrows(file) }

                ex.exceptionResponseCode shouldBe REQUIRE_FILE_EXTENSION
                Files.deleteIfExists(Paths.get(storagePath))
            }

            test("허용되지 않은 확장자면 INVALID_FILE_EXTENSION 예외를 던진다") {
                val storagePath = Files.createTempDirectory("fileadapter").toString()
                val props = FileStorageProperties(path = storagePath, cachePeriod = 3600)
                val adapter = FileAdapter(props)
                val file = MockMultipartFile(
                    "audio",
                    "retelling.png",
                    "image/png",
                    "audio-content".toByteArray()
                )

                val ex = shouldThrow<BadRequestException> { adapter.saveAudioOrThrows(file) }

                ex.exceptionResponseCode shouldBe INVALID_FILE_EXTENSION
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
