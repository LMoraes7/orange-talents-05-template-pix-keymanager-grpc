package br.com.zup.edu.grpc.handler

import br.com.zup.edu.grpc.dominio.exception.ChavePixDuplicadaException
import br.com.zup.edu.grpc.dominio.exception.ContaInexisteException
import br.com.zup.edu.grpc.dominio.exception.ValidacaoException
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorAroundHandler::class)
class ErrorHandlerInterceptor: MethodInterceptor<Any, Any> {

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {
        try {
            return context.proceed()
        } catch (ex: Exception) {
            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            val status = when (ex) {
                is ConstraintViolationException,
                is ValidacaoException -> Status.INVALID_ARGUMENT
                    .withCause(ex)
                    .withDescription(ex.message)

                is ChavePixDuplicadaException -> Status.ALREADY_EXISTS
                    .withCause(ex)
                    .withDescription(ex.message)

                is ValidacaoException -> Status.INTERNAL
                    .withCause(ex)
                    .withDescription(ex.message)

                is ContaInexisteException -> Status.NOT_FOUND
                    .withCause(ex)
                    .withDescription(ex.message)

                else -> {
                    //logger.error(ex.stackTraceToString())
                    //logger.error(ex.message)
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