package br.com.zup.edu.grpc.http.client.itau.response

import br.com.zup.edu.grpc.dominio.modelo.Instuicao

data class InstuicaoResponse(val nome: String, val ispb: String) {

    fun paraInstituicaoModel(): Instuicao =
        Instuicao(
            nome = this.nome,
            ispb = this.ispb
        )

}