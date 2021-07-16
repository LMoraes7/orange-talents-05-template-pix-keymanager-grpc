package br.com.zup.edu.grpc.http.client.bcb.dto.response

data class BankAccountResponse(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: String
)
