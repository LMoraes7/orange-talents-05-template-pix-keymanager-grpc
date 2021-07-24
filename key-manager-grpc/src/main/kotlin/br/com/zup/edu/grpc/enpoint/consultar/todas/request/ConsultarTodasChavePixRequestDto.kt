package br.com.zup.edu.grpc.enpoint.consultar.todas.request

import br.com.zup.edu.grpc.dominio.validacao.ValidarUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
class ConsultarTodasChavePixRequestDto(
    @field:NotBlank
    @field:ValidarUUID
    val clienteId: String?
)
