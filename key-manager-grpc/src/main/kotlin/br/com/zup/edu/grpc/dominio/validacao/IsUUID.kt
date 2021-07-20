package br.com.zup.edu.grpc.dominio.validacao

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.ReportAsSingleViolation
import javax.validation.constraints.Pattern
import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

@ReportAsSingleViolation
@Constraint(validatedBy = [IsUUIDValidator::class])
//@Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
//    flags = [Pattern.Flag.CASE_INSENSITIVE])
@Target(FIELD)
@Retention(RUNTIME)
annotation class IsUUID(
    val message: String = "O id informado não é válido",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = [],
)

@Singleton
class IsUUIDValidator : ConstraintValidator<IsUUID, String> {
    override fun isValid(
        value: String?,
        annotationMetadata: AnnotationValue<IsUUID>,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (value.isNullOrBlank())
            return false

        val regex: Regex =
            "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\$".toRegex()

        return value.matches(regex)
    }
}
