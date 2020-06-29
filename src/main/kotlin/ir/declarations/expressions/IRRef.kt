package ir.declarations.expressions

import buildPrettyString
import ir.visitors.IRElementVisitor
import ir.declarations.IRExpression
import ir.declarations.IRStatementContainer
import ir.symbol.IRRefSymbol
import ir.symbol.IRSymbol
import ir.symbol.IRSymbolOwner
import ir.types.IRType
import ir.visitors.IRElementTransformer

class IRRef(
    val refName: String,
    override val type: IRType,
    override val symbol: IRRefSymbol,
    override var parent: IRStatementContainer?
) : IRExpression, IRSymbolOwner{
    init{
        symbol.bind(this)
    }

    override fun <R, D> accept(visitor: IRElementVisitor<R, D>, data: D): R =
        visitor.visitRef(this, data)

    override fun <D> transformChildren(transformer: IRElementTransformer<D>, data: D) {
        //No children
    }

    override fun toString(): String =
        buildPrettyString {
            append("%$refName")
        }

}