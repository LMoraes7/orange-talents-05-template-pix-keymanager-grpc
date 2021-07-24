package br.com.zup.edu.grpc.http.client.bcb.request

import br.com.zup.edu.grpc.dominio.enums.TipoContaModel

enum class AccountType {

    CACC {
        override fun getTipoConta(): TipoContaModel = TipoContaModel.CONTA_CORRENTE
    },
    SVGS {
        override fun getTipoConta(): TipoContaModel = TipoContaModel.CONTA_POUPANCA
    };

    abstract fun getTipoConta(): TipoContaModel
}
