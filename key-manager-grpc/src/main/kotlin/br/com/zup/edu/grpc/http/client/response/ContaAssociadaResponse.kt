package br.com.zup.edu.grpc.http.client.response

data class ContaAssociadaResponse(
    val tipo: String,
    val instituicao: InstuicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
)
