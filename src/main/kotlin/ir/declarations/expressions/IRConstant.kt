package ir.declarations.expressions

import buildPrettyString
import ir.visitors.IRElementVisitor
import ir.builtin.BuiltinTypes
import ir.declarations.IRExpression
import ir.declarations.IRStatementContainer
import ir.types.IRType
import ir.visitors.IRElementTransformer

class IRConstant<T>(
    override val type: IRType,
    val kind: IRConstantKind<T>,
    val value: T,
    override var parent: IRStatementContainer?
):
    IRExpression {
    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitConstant(this, data)

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        //No children
    }

    companion object{
        fun integer(int: Int, parent: IRStatementContainer?) = IRConstant(
            BuiltinTypes.INT.makeSimpleType(),
            IRConstantKind.Int,
            int,
            parent
        )
    }

    override fun toString(): String  =
        buildPrettyString{
            when(kind){
                is IRConstantKind.Int -> append("int $value")
            }
        }

}

sealed class IRConstantKind<T>(val asString: String){
    object Int : IRConstantKind<kotlin.Int>("Int")
}