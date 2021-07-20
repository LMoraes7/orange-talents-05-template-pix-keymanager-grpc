package br.com.zup.edu.grpc.http.client.bcb.dto.response

import br.com.zup.edu.grpc.endpoint.consultar.dto.response.ChavePixResponseDto
import br.com.zup.edu.grpc.endpoint.consultar.dto.response.ContaAssociadaResponseDto
import br.com.zup.edu.grpc.http.client.bcb.dto.request.AccountType
import br.com.zup.edu.grpc.http.client.bcb.dto.request.KeyType

data class PixKeyDetailsResponse(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccountResponse,
    val owner: OwnerResponse,
    val createdAt: String,
) {
    fun paraChavePixResponseDto(): ChavePixResponseDto =
        ChavePixResponseDto(
            tipoChave = KeyType.valueOf(this.keyType).getTipoChave().name,
            chave = this.key,
            titular = this.owner.paraTitularResponseDto(),
            contaAssociada = ContaAssociadaResponseDto(
                instituicao = "Itaú Unibanco",
                agencia = this.bankAccount.branch,
                numero = this.bankAccount.accountNumber,
                tipoConta = AccountType.valueOf(this.bankAccount.accountType)
                    .getTipoConta().name
            ),
            dataRegistro = this.createdAt
        )
}