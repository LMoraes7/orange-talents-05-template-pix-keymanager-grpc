package br.com.zup.edu.grpc.dominio.exception

open abstract class DadoInvalidoException(message: String? = null) : Exception(message)

class ContaInvalidaException(message: String? = null) : DadoInvalidoException(message)

class EnumInvalidaException(message: String? = null) : DadoInvalidoException(message)

class DadosDeEntrandaInvalidosException(message: String? = null) : DadoInvalidoException(message)
