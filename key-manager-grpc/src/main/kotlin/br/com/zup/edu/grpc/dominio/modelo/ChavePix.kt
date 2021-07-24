package br.com.zup.edu.grpc.dominio.modelo

import br.com.zup.edu.grpc.dominio.enums.TipoChaveModel
import br.com.zup.edu.grpc.dominio.enums.TipoContaModel
import br.com.zup.edu.grpc.http.client.bcb.request.CreatePixKeyRequest
import br.com.zup.edu.grpc.http.client.bcb.request.DeletePixKeyRequest
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
class ChavePix(
    @Column(nullable = false)
    val clienteId: String,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tipoChave: TipoChaveModel,
    @Column(nullable = false, unique = true)
    val chave: String,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tipoConta: TipoContaModel,
    @Embedded
    val conta: Conta
) {

    //A PRÓPRIA APLICAÇÃO ESTÁ RESPONSÁVEL POR GERAR O ID
    @Id
    var identificador: String? = (UUID.randomUUID().toString() + LocalDateTime.now().toString())

    @Column(nullable = false, unique = true)
    val idInterno: String = UUID.randomUUID().toString()
    //------------------------------------

    @Column(nullable = false, columnDefinition = "DATETIME")
    val registradaEm: LocalDateTime = LocalDateTime.now()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChavePix

        if (chave != other.chave) return false
        if (identificador != other.identificador) return false
        if (idInterno != other.idInterno) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chave.hashCode()
        result = 31 * result + (identificador?.hashCode() ?: 0)
        result = 31 * result + idInterno.hashCode()
        return result
    }

    fun paraCreatePixKeyRequest(): CreatePixKeyRequest =
        CreatePixKeyRequest(this)

    fun paraDeletePixKeyRequest(): DeletePixKeyRequest =
        DeletePixKeyRequest(this)
}

@Embeddable
class Conta(
    @Column(nullable = false, name = "conta_tipo")
    val tipo: String,
    @Embedded
    val instituicao: Instituicao,
    @Column(nullable = false, name = "conta_agencia")
    val agencia: String,
    @Column(nullable = false, name = "conta_numero")
    val numero: String,
    @Embedded
    val titular: Titular,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Conta

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
}

@Embeddable
class Instituicao(
    @Column(nullable = false, name = "instituicao_nome")
    val nome: String,
    @Column(nullable = false, name = "instituicao_ispb")
    val ispb: String,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Instituicao

        if (ispb != other.ispb) return false

        return true
    }

    override fun hashCode(): Int {
        return ispb.hashCode()
    }
}

@Embeddable
class Titular(
    @Column(nullable = false, name = "titular_nome")
    val nome: String,
    @Column(nullable = false, name = "titular_cpf")
    val cpf: String,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Titular

        if (nome != other.nome) return false

        return true
    }

    override fun hashCode(): Int {
        return nome.hashCode()
    }
}
