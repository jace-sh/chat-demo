package jace.shim.webfluxfun.domain.idgenerate

/**
 * Entity 식별자 생성 인터페이스
 *
 * @param <T>
</T> */
interface IdentifierGenerator<T> {
    fun generate(): T
}