package br.com.zup.edu.grpc.http.client.itau.response

import br.com.zup.edu.grpc.dominio.modelo.ContaAssociada

data class ContaAssociadaResponse(
    val tipo: String,
    val instituicao: InstuicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
) {
    fun paraContaAssociadaModel() =
        ContaAssociada(
            tipo = this.tipo,
            instituicao = this.instituicao.paraInstituicaoModel(),
            agencia = this.agencia,
            numero = this.numero,
            titular = this.titular.paraTitularModel()
        )
}
