package br.com.zup.edu.grpc.http.client.bcb.response

import br.com.zup.edu.grpc.http.client.bcb.request.BankAccount
import br.com.zup.edu.grpc.http.client.bcb.request.Owner

class CreatePixKeyResponse(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: String
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreatePixKeyResponse

        if (keyType != other.keyType) return false

        return true
    }

    override fun hashCode(): Int {
        return keyType.hashCode()
    }
}
