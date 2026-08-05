package com.krince.reminisce.application.service.child

import com.krince.reminisce.application.port.`in`.child.command.GetChildCommand
import com.krince.reminisce.application.port.`in`.child.command.GetChildrenCommand
import com.krince.reminisce.application.port.`in`.child.command.RegisterChildCommand
import com.krince.reminisce.application.port.`in`.child.result.ChildResult
import com.krince.reminisce.application.port.`in`.child.usecase.GetChildUseCase
import com.krince.reminisce.application.port.`in`.child.usecase.GetChildrenUseCase
import com.krince.reminisce.application.port.`in`.child.usecase.RegisterChildUseCase
import com.krince.reminisce.application.port.out.child.CommandChildPort
import com.krince.reminisce.application.port.out.child.LoadChildPort
import com.krince.reminisce.application.port.out.childconsent.CommandChildConsentPort
import com.krince.reminisce.application.validator.child.RegisterChildValidator
import com.krince.reminisce.domain.model.child.Child
import com.krince.reminisce.domain.model.child.vo.BirthYear
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.child.vo.ChildNickname
import com.krince.reminisce.domain.model.childconsent.ChildConsent
import com.krince.reminisce.domain.model.childconsent.vo.ConsentVersion
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.infra.config.properties.ChildPolicyProperties
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_CHILD
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime
import java.time.Year

@Service
@EnableConfigurationProperties(ChildPolicyProperties::class)
class ChildApplicationService(
    private val loadChildPort: LoadChildPort,
    private val commandChildPort: CommandChildPort,
    private val commandChildConsentPort: CommandChildConsentPort,
    private val childPolicyProperties: ChildPolicyProperties,
    private val clock: Clock,
) : RegisterChildUseCase, GetChildrenUseCase, GetChildUseCase {

    @Transactional
    override fun execute(command: RegisterChildCommand): ChildResult {
        val guardianId = UserId(command.guardianId)
        val currentCount: Long = loadChildPort.countByGuardianId(guardianId)
        RegisterChildValidator.validateWithinLimit(currentCount, childPolicyProperties.maxPerGuardian)

        val birthYear = BirthYear(command.birthYear)
        RegisterChildValidator.validateBirthYearNotInFuture(command.birthYear, Year.now(clock).value)

        val child: Child = Child.register(guardianId, ChildNickname(command.nickname), birthYear)
        val savedChild: Child = commandChildPort.save(child)

        val consent: ChildConsent = ChildConsent.givenByAuthenticatedParent(
            childId = savedChild.childId,
            consentVersion = ConsentVersion(command.consentVersion),
            consentedAt = LocalDateTime.now(clock),
        )
        commandChildConsentPort.save(consent)

        return ChildResult.from(savedChild)
    }

    @Transactional(readOnly = true)
    override fun execute(command: GetChildrenCommand): List<ChildResult> {
        val guardianId = UserId(command.guardianId)

        return loadChildPort.findAllByGuardianId(guardianId).map { ChildResult.from(it) }
    }

    @Transactional(readOnly = true)
    override fun execute(command: GetChildCommand): ChildResult {
        val guardianId = UserId(command.guardianId)
        val child: Child = loadChildPort.findById(ChildId(command.childId))
            ?: throw NotFoundException(NOT_FOUND_CHILD, NOT_FOUND_CHILD.message)

        if (child.guardianId != guardianId) {
            throw NotFoundException(NOT_FOUND_CHILD, NOT_FOUND_CHILD.message)
        }

        return ChildResult.from(child)
    }
}
