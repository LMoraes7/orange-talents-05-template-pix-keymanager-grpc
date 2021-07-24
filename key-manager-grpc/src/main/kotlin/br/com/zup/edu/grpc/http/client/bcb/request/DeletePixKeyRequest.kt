package br.com.zup.edu.grpc.http.client.bcb.request

import br.com.zup.edu.grpc.dominio.modelo.ChavePix

class DeletePixKeyRequest(
    chavePix: ChavePix
) {
    val key: String = chavePix.chave
    val participant: String = chavePix.conta.instituicao.ispb
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeletePixKeyRequest

        if (participant != other.participant) return false

        return true
    }

    override fun hashCode(): Int {
        return participant.hashCode()
    }


}
