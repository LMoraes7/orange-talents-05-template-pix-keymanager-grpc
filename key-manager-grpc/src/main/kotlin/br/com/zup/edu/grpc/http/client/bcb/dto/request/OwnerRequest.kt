package br.com.zup.edu.grpc.http.client.bcb.dto.request

data class OwnerRequest(
    val type: String, //PESSOA FÍSICA OU JURÍDICA
    val name: String, //NOME
    val taxIdNumber: String //CPF OU CNPJ
)