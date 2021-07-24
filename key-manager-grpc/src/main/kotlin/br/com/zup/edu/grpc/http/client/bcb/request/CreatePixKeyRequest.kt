package br.com.zup.edu.grpc.http.client.bcb.request

import br.com.zup.edu.grpc.dominio.modelo.ChavePix

class CreatePixKeyRequest(
    chavePix: ChavePix
) {
    val keyType: String = chavePix.tipoChave.getKeyType().name
    val key: String = chavePix.chave
    val bankAccount: BankAccount = BankAccount(
        participant = chavePix.conta.instituicao.ispb,
        branch = chavePix.conta.agencia,
        accountNumber = chavePix.conta.numero,
        accountType = chavePix.tipoConta.getAccountType().name
    )
    val owner: Owner = Owner(
        type = "NATURAL_PERSON",
        name = chavePix.conta.titular.nome,
        taxIdNumber = chavePix.conta.titular.cpf
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreatePixKeyRequest

        if (keyType != other.keyType) return false

        return true
    }

    override fun hashCode(): Int {
        return keyType.hashCode()
    }


}

class Owner(
    val type: String,
    val name: String,
    val taxIdNumber: String
)

class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: String
)
