package br.com.zup.edu.grpc.http.client.bcb.dto.response

data class OwnerResponse(
    val type: String,
    val name: String,
    val taxIdNumber: String
)
