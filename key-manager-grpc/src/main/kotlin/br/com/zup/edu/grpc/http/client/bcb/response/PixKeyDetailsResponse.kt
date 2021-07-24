package br.com.zup.edu.grpc.http.client.bcb.response

import br.com.zup.edu.grpc.enpoint.consultar.unica.request.ChavePixResponseDto
import br.com.zup.edu.grpc.enpoint.consultar.unica.request.ContaAssociadaResponseDto
import br.com.zup.edu.grpc.enpoint.consultar.unica.request.TitularResponseDto
import br.com.zup.edu.grpc.http.client.bcb.request.AccountType
import br.com.zup.edu.grpc.http.client.bcb.request.BankAccount
import br.com.zup.edu.grpc.http.client.bcb.request.KeyType
import br.com.zup.edu.grpc.http.client.bcb.request.Owner

class PixKeyDetailsResponse(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: String,
) {

    fun paraChavePixResponseDto(): ChavePixResponseDto =
        ChavePixResponseDto(
            tipoChave = KeyType.valueOf(this.keyType).getTipoChave().name,
            chave = this.key,
            titular = TitularResponseDto(
                nome = this.owner.name,
                cpf = this.owner.taxIdNumber
            ),
            conta = ContaAssociadaResponseDto(
                instituicao = "Itaú Unibanco",
                agencia = this.bankAccount.branch,
                numero = this.bankAccount.accountNumber,
                tipoConta = AccountType.valueOf(this.bankAccount.accountType).getTipoConta().name
            ),
            registradaEm = this.createdAt
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PixKeyDetailsResponse

        if (keyType != other.keyType) return false

        return true
    }

    override fun hashCode(): Int {
        return keyType.hashCode()
    }
}
