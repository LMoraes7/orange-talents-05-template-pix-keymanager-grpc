package br.com.zup.edu.grpc.http.client.itau.response

import br.com.zup.edu.grpc.dominio.modelo.Conta
import br.com.zup.edu.grpc.dominio.modelo.Instituicao
import br.com.zup.edu.grpc.dominio.modelo.Titular

data class DadosDaContaResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DadosDaContaResponse

        if (tipo != other.tipo) return false
        if (instituicao != other.instituicao) return false
        if (agencia != other.agencia) return false
        if (numero != other.numero) return false
        if (titular != other.titular) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tipo.hashCode()
        result = 31 * result + instituicao.hashCode()
        result = 31 * result + agencia.hashCode()
        result = 31 * result + numero.hashCode()
        result = 31 * result + titular.hashCode()
        return result
    }

    fun paraContaModel() =
        Conta(
            tipo = this.tipo,
            instituicao = this.instituicao.paraInstituicaoModel(),
            agencia = this.agencia,
            numero = this.numero,
            titular = this.titular.paraTitularModel()
        )

}

data class TitularResponse(
    val id: String,
    val nome: String,
    val cpf: String,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TitularResponse

        if (nome != other.nome) return false
        if (cpf != other.cpf) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nome.hashCode()
        result = 31 * result + cpf.hashCode()
        return result
    }

    fun paraTitularModel() =
        Titular(
            nome = this.nome,
            cpf = this.cpf
        )

}

data class InstituicaoResponse(
    val nome: String,
    val ispb: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InstituicaoResponse

        if (nome != other.nome) return false
        if (ispb != other.ispb) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nome.hashCode()
        result = 31 * result + ispb.hashCode()
        return result
    }

    fun paraInstituicaoModel() =
        Instituicao(
            nome = this.nome,
            ispb = this.ispb
        )
}