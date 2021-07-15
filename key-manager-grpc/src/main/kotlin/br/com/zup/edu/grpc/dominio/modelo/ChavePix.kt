package br.com.zup.edu.grpc.dominio.modelo

import br.com.zup.edu.grpc.dominio.enums.TipoChaveModel
import br.com.zup.edu.grpc.dominio.enums.TipoContaModel
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
    val conta: ContaAssociada,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, unique = true)
    val pixIdInterno: String = UUID.randomUUID().toString()
}

@Embeddable
data class ContaAssociada(
    @Column(nullable = false, name = "conta_tipo")
    val tipo: String,
    @Embedded
    val instituicao: Instuicao,
    @Column(nullable = false, name = "conta_agencia")
    val agencia: String,
    @Column(nullable = false, name = "conta_numero")
    val numero: String,
    @Embedded
    val titular: Titular,
)

@Embeddable
data class Instuicao(
    @Column(nullable = false, name = "instituicao_nome")
    val nome: String,
    @Column(nullable = false, name = "instituicao_ispb")
    val ispb: String,
)

@Embeddable
data class Titular(
    @Column(nullable = false, name = "titular_id")
    val id: String,
    @Column(nullable = false, name = "titular_nome")
    val nome: String,
    @Column(nullable = false, name = "titular_cpf")
    val cpf: String,
)
