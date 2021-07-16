package br.com.zup.edu.grpc.endpoint.remover.dto

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
class ChavePixRemoverRequestDto(
    @field:NotBlank
    val clienteId: String?,
    @field:NotBlank
    val pixIdInterno: String?,
) {

}
