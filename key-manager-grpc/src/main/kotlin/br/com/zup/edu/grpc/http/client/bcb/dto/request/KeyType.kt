package br.com.zup.edu.grpc.http.client.bcb.dto.request

import br.com.zup.edu.grpc.dominio.enums.TipoChaveModel

enum class KeyType {

    CPF {
        override fun getTipoChave(): TipoChaveModel = TipoChaveModel.CPF
    },
    CNPJ {
        override fun getTipoChave(): TipoChaveModel = TipoChaveModel.CNPJ
    },
    PHONE {
        override fun getTipoChave(): TipoChaveModel = TipoChaveModel.CELULAR
    },
    EMAIL {
        override fun getTipoChave(): TipoChaveModel = TipoChaveModel.EMAIL
    },
    RANDOM {
        override fun getTipoChave(): TipoChaveModel = TipoChaveModel.ALEATORIA
    };

    abstract fun getTipoChave(): TipoChaveModel
}
