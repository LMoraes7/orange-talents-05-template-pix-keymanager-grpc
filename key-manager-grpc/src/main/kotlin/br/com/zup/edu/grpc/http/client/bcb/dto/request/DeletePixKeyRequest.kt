package br.com.zup.edu.grpc.http.client.bcb.dto.request

import br.com.zup.edu.grpc.dominio.modelo.ChavePix

class DeletePixKeyRequest(
    chavePix: ChavePix
) {

    val key: String = chavePix.chave
    val participant: String = chavePix.conta.instituicao.ispb
}
