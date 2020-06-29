package ir.types

import buildPrettyString
import ir.builtin.BuiltinTypes
import ir.symbol.IRSymbol

interface TypeMarker

interface IRType : TypeMarker{
    companion object{
        val default = BuiltinTypes.DYNAMIC.makeSimpleType()
    }
}
interface IRSimpleTypeMarker: TypeMarker
interface IRDynamicTypeMarker: TypeMarker
interface IRUnresolvedTypeMarker: TypeMarker

class IRSimpleType(val symbol: String) : IRType, IRSimpleTypeMarker{
    override fun toString(): String =
        buildPrettyString{
            append(symbol)
        }
}

interface IRDynamicType : IRType, IRDynamicTypeMarker