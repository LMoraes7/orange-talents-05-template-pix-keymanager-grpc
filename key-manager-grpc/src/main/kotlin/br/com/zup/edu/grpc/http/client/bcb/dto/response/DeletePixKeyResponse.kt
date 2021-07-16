package br.com.zup.edu.grpc.http.client.bcb.dto.response

data class DeletePixKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: String
) {

}
