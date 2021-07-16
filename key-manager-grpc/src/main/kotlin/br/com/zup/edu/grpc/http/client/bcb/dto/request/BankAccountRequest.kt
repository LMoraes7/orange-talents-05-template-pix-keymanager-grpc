package br.com.zup.edu.grpc.http.client.bcb.dto.request

class BankAccountRequest(
    val participant: String, //ISPB
    val branch: String, // AGÊNCIA
    val accountNumber: String, // Nº CONTA
    val accountType: String //TIPO DA CONTA
) {

}