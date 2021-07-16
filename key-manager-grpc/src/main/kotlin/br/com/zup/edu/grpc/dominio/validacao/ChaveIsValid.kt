package br.com.zup.edu.grpc.dominio.validacao

import br.com.zup.edu.grpc.endpoint.cadastrar.dto.ChavePixCadastrarRequestDto
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint
import kotlin.annotation.AnnotationTarget.*


@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ChaveIsValidValidator::class])
@Target(CLASS)
annotation class ChaveIsValid

@Singleton
class ChaveIsValidValidator : ConstraintValidator<ChaveIsValid, ChavePixCadastrarRequestDto> {

    override fun isValid(
        value: ChavePixCadastrarRequestDto,
        annotationMetadata: AnnotationValue<ChaveIsValid>,
        context: ConstraintValidatorContext,
    ): Boolean {
        return value.tipoChave.valida(value.chave)
    }
}