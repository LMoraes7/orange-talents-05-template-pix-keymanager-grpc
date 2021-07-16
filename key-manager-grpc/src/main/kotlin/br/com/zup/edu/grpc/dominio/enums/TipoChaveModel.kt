package br.com.zup.edu.grpc.dominio.enums

import br.com.zup.edu.grpc.http.client.bcb.dto.request.KeyType
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class TipoChaveModel {
    CPF {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank())
                return false

            return CPFValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }

        override fun keyType(): KeyType = KeyType.CPF
    },
    CELULAR {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank())
                return false

            return chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }

        override fun keyType(): KeyType = KeyType.PHONE
    },
    EMAIL {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank())
                return false

            return EmailValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }

        override fun keyType(): KeyType = KeyType.EMAIL
    },
    ALEATORIA {
        override fun valida(chave: String?): Boolean = chave.isNullOrBlank()

        override fun keyType(): KeyType = KeyType.RANDOM
    };

    abstract fun valida(chave: String?): Boolean

    abstract fun keyType(): KeyType
}