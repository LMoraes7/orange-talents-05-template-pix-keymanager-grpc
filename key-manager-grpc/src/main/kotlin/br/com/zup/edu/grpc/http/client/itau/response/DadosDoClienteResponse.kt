package br.com.zup.edu.grpc.http.client.itau.response

class DadosDoClienteResponse(
    val id: String,
    val nome: String,
    val cpf: String,
    val instituicao: InstituicaoResponse
)
