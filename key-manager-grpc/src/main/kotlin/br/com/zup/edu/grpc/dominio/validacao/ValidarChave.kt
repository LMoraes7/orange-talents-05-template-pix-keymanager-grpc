package br.com.zup.edu.grpc.dominio.validacao

import br.com.zup.edu.grpc.enpoint.cadastrar.dto.NovaChavePixRequestDto
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.validation.Constraint
import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.*

@MustBeDocumented
@Retention(RUNTIME)
@Constraint(validatedBy = [ValidarChaveValidator::class])
@Target(CLASS)
annotation class ValidarChave

class ValidarChaveValidator: ConstraintValidator<ValidarChave, NovaChavePixRequestDto> {

    override fun isValid(
        value: NovaChavePixRequestDto?,
        annotationMetadata: AnnotationValue<ValidarChave>,
        context: ConstraintValidatorContext,
    ): Boolean {
        return value!!.tipoChave.valida(value.chave)
    }
}
