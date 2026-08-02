package com.krince.reminisce.shared.util

import com.krince.reminisce.shared.context.RequestContext
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.slf4j.MDC

@Tags("test", "unitTest")
@DisplayName("LoggingUtil 단위테스트")
class LoggingUtilTest : FunSpec({

    afterEach {
        LoggingUtil.clearContext()
    }

    context("setRequestContext") {
        context("성공") {
            test("RequestContext 값을 MDC에 넣는다") {
                val context = RequestContext(
                    requestId = "req-1",
                    userId = "user-1",
                    clientIp = "127.0.0.1",
                    userAgent = "TestAgent",
                    queryString = "a=1&b=2",
                )
                LoggingUtil.setRequestContext(context)
                MDC.get("requestId") shouldBe "req-1"
                MDC.get("userId") shouldBe "user-1"
                MDC.get("clientIp") shouldBe "127.0.0.1"
                MDC.get("userAgent") shouldBe "TestAgent"
                MDC.get("queryString") shouldBe "a=1&b=2"
            }
            test("userId가 null이면 GUEST를 넣는다") {
                val context = RequestContext(
                    requestId = "req-2",
                    userId = null,
                    clientIp = null,
                    userAgent = null,
                    queryString = null,
                )
                LoggingUtil.setRequestContext(context)
                MDC.get("userId") shouldBe "GUEST"
                MDC.get("clientIp") shouldBe "UNKNOWN"
                MDC.get("userAgent") shouldBe "UNKNOWN"
                MDC.get("queryString") shouldBe ""
            }
            test("queryString이 500자 초과면 잘라서 뒤에 ...을 붙인다") {
                val longQuery = "x".repeat(600)
                val context = RequestContext(
                    requestId = "req-3",
                    queryString = longQuery,
                )
                LoggingUtil.setRequestContext(context)
                val stored = MDC.get("queryString")
                stored!!.length shouldBe 503
                stored shouldBe "x".repeat(500) + "..."
            }
            test("queryString이 null이거나 blank면 빈 문자열을 넣는다") {
                val context = RequestContext(
                    requestId = "req-4",
                    queryString = null,
                )
                LoggingUtil.setRequestContext(context)
                MDC.get("queryString") shouldBe ""
                LoggingUtil.clearContext()
                val contextBlank = RequestContext(
                    requestId = "req-5",
                    queryString = "   ",
                )
                LoggingUtil.setRequestContext(contextBlank)
                MDC.get("queryString") shouldBe ""
            }
        }
    }

    context("clearContext") {
        context("성공") {
            test("MDC를 비운다") {
                val context = RequestContext(requestId = "req-6")
                LoggingUtil.setRequestContext(context)
                MDC.get("requestId") shouldBe "req-6"
                LoggingUtil.clearContext()
                MDC.get("requestId") shouldBe null
            }
        }
    }
})
