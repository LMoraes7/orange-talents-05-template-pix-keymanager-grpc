package br.com.zup.edu.grpc.http.client

data class ContaAssociadaResponse(
    val tipo: String,
    val instituicao: InstuicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
) {

}

data class InstuicaoResponse(val nome: String, val ispb: String)
data class  TitularResponse(val id: String, val nome: String, val cpf: String)
