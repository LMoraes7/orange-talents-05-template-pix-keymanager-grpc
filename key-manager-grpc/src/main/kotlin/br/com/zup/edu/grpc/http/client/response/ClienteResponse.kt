package br.com.zup.edu.grpc.http.client.response

data class ClienteResponse(
    val id: String,
    val nome: String,
    val cpf: String,
    val instituicao: InstuicaoResponse,
)
