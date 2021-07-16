package br.com.zup.edu.grpc.http.client.bcb.dto.response

data class CreatePixKeyResponse(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccountResponse,
    val owner: 	OwnerResponse,
    val createdAt: String
)
