package br.com.zup.edu.grpc.http.client.bcb.dto.response

import br.com.zup.edu.grpc.dominio.modelo.Titular
import br.com.zup.edu.grpc.endpoint.consultar.dto.response.TitularResponseDto

data class OwnerResponse(
    val type: String,
    val name: String,
    val taxIdNumber: String
) {

    fun paraTitularResponseDto(): TitularResponseDto =
        TitularResponseDto(
            nome = this.name,
            cpf = this.taxIdNumber
        )
}
