package br.com.zup.edu.grpc.handler

import br.com.zup.edu.grpc.dominio.exception.badrequest.ChavePixDuplicadaException
import br.com.zup.edu.grpc.dominio.exception.badrequest.ValidacaoException
import br.com.zup.edu.grpc.dominio.exception.forbidden.ManipulacaoInvalidaDeRecursoException
import br.com.zup.edu.grpc.dominio.exception.notfound.InformacaoNaoEncontradaException
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorAroundHandler::class)
class ErrorHandlerInterceptor : MethodInterceptor<Any, Any> {

    private val logger: Logger = LoggerFactory.getLogger(ErrorHandlerInterceptor::class.java)

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {
        try {
            return context.proceed()
        } catch (ex: Exception) {
            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            val status = when (ex) {
                is ConstraintViolationException -> {
                    this.logger.error(ex.stackTraceToString())
                    this.logger.error(ex.message)
                    Status.INVALID_ARGUMENT
                        .withCause(ex)
                        .withDescription(ex.message)
                }

                is ValidacaoException -> {
                    this.logger.error(ex.stackTraceToString())
                    this.logger.error(ex.message)
                    Status.INVALID_ARGUMENT
                        .withCause(ex)
                        .withDescription(ex.message)
                }

                is ChavePixDuplicadaException -> {
                    this.logger.error(ex.stackTraceToString())
                    this.logger.error(ex.message)
                    Status.ALREADY_EXISTS
                        .withCause(ex)
                        .withDescription(ex.message)
                }

                is InformacaoNaoEncontradaException -> {
                    this.logger.error(ex.stackTraceToString())
                    this.logger.error(ex.message)
                    Status.NOT_FOUND
                        .withCause(ex)
                        .withDescription(ex.message)
                }

                is ManipulacaoInvalidaDeRecursoException -> {
                    this.logger.error(ex.stackTraceToString())
                    this.logger.error(ex.message)
                    Status.PERMISSION_DENIED
                        .withCause(ex)
                        .withDescription(ex.message)
                }

                else -> {
                    this.logger.error(ex.stackTraceToString())
                    this.logger.error(ex.message)
                    Status.UNKNOWN
                        .withCause(ex)
                        .withDescription("Ops, um erro inesperado ocorreu")
                }
            }

            responseObserver.onError(status.asRuntimeException())
        }
        return null
    }
}