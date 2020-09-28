import arrow.core.*
import java.io.File
import javax.xml.crypto.Data
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor



class Executable(private val instructions: ByteArray): Iterator<Byte>{
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Executable

        if (!instructions.contentEquals(other.instructions)) return false

        return true
    }

    var instructionPtr = 0

    override fun hashCode(): Int {
        return instructions.contentHashCode()
    }

    @ExperimentalStdlibApi
    override fun toString(): String =
        buildPrettyString {
            instructions.withIndex().forEach { (idx, it) ->
                append("0x${Integer.toHexString(it.toInt())}")
                if(idx < instructions.size - 1){
                    append(", ")
                }
            }
        }

    override fun hasNext(): Boolean = instructionPtr < instructions.size

    override fun next(): Byte = instructions[instructionPtr++]

    fun nextByte() = DataType.Byte(instructions[instructionPtr++])
    fun nextWord() = DataType.Word(DataType.Byte(instructions[instructionPtr++]), DataType.Byte(instructions[instructionPtr++]))
    fun nextDoubleWord() = DataType.DoubleWord(
        DataType.Word(
            DataType.Byte(instructions[instructionPtr++]),
            DataType.Byte(instructions[instructionPtr++])
        ),
        DataType.Word(
            DataType.Byte(instructions[instructionPtr++]),
            DataType.Byte(instructions[instructionPtr++])
        )
    )

    fun nextQuadWord() = DataType.QuadWord(
        DataType.DoubleWord(
            DataType.Word(
                DataType.Byte(instructions[instructionPtr++]),
                DataType.Byte(instructions[instructionPtr++])
            ),
            DataType.Word(
                DataType.Byte(instructions[instructionPtr++]),
                DataType.Byte(instructions[instructionPtr++])
            )
        ),
        DataType.DoubleWord(
            DataType.Word(
                DataType.Byte(instructions[instructionPtr++]),
                DataType.Byte(instructions[instructionPtr++])
            ),
            DataType.Word(
                DataType.Byte(instructions[instructionPtr++]),
                DataType.Byte(instructions[instructionPtr++])
            )
        )
    )

}

class Stack{
    private val stack: ByteArray = ByteArray(1024)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Stack

        if (!stack.contentEquals(other.stack)) return false

        return true
    }

    private var stackPtr = 0

    val top: Byte
        get() = if(stackPtr == 0) 0 else stack[stackPtr - 1]

    override fun hashCode(): Int {
        return stack.contentHashCode()
    }

    fun push(data: DataType){
        when(data){
            is DataType.Byte -> stack[stackPtr++] = data.data
            is DataType.Word -> {
                stack[stackPtr++] = data.data1.data
                stack[stackPtr++] = data.data2.data
            }
            is DataType.DoubleWord -> {
                stack[stackPtr++] = data.data1.data1.data
                stack[stackPtr++] = data.data1.data2.data
                stack[stackPtr++] = data.data2.data1.data
                stack[stackPtr++] = data.data2.data2.data
            }
            is DataType.QuadWord -> {
                stack[stackPtr++] = data.data1.data1.data1.data
                stack[stackPtr++] = data.data1.data1.data2.data
                stack[stackPtr++] = data.data1.data2.data1.data
                stack[stackPtr++] = data.data1.data2.data2.data
                stack[stackPtr++] = data.data2.data1.data1.data
                stack[stackPtr++] = data.data2.data1.data2.data
                stack[stackPtr++] = data.data2.data2.data1.data
                stack[stackPtr++] = data.data2.data2.data2.data
            }
        }
    }

    fun pop(){
        stack[--stackPtr] = 0
    }

}

class Memory{
    val memory = ByteArray(1024)

    fun writeByte(dest: Byte, target: DataType.Byte){
        memory[dest.toInt()] = target.data
    }

    fun writeWord(dest: Byte, target: DataType.Word){
        memory[dest.toInt()] = target.data1.data
        memory[dest.toInt() + 1] = target.data2.data
    }

    fun writeDouble(dest: Byte, target: DataType.DoubleWord){
        memory[dest.toInt()] = target.data1.data1.data
        memory[dest.toInt()+1] = target.data1.data2.data
        memory[dest.toInt()+2] = target.data2.data1.data
        memory[dest.toInt()+3] = target.data2.data2.data
    }
    fun writeQuad(dest: Byte, target: DataType.QuadWord){
        memory[dest.toInt()] = target.data1.data1.data1.data
        memory[dest.toInt()+1] = target.data1.data1.data2.data
        memory[dest.toInt()+2] = target.data1.data2.data1.data
        memory[dest.toInt()+3] = target.data1.data2.data2.data
        memory[dest.toInt()+4] = target.data2.data1.data1.data
        memory[dest.toInt()+5] = target.data2.data1.data2.data
        memory[dest.toInt()+6] = target.data2.data2.data1.data
        memory[dest.toInt()+7] = target.data2.data2.data2.data
    }

    fun readByte(dest: Byte) = DataType.Byte(memory[dest.toInt()])
    fun readWord(dest: Byte) = DataType.Word(DataType.Byte(memory[dest.toInt()]), DataType.Byte(memory[dest.toInt()+1]))
    fun readDoubleWord(dest: Byte) = DataType.DoubleWord(
        DataType.Word(
            DataType.Byte(memory[dest.toInt()]),
            DataType.Byte(memory[dest.toInt()+1])
        ),
        DataType.Word(
            DataType.Byte(memory[dest.toInt()+2]),
            DataType.Byte(memory[dest.toInt()+3])
        )
    )

    fun readQuadWord(dest: Byte) = DataType.QuadWord(
        DataType.DoubleWord(
            DataType.Word(
                DataType.Byte(memory[dest.toInt()]),
                DataType.Byte(memory[dest.toInt()+1])
            ),
            DataType.Word(
                DataType.Byte(memory[dest.toInt()+2]),
                DataType.Byte(memory[dest.toInt()+3])
            )
        ),
        DataType.DoubleWord(
            DataType.Word(
                DataType.Byte(memory[dest.toInt()+4]),
                DataType.Byte(memory[dest.toInt()+5])
            ),
            DataType.Word(
                DataType.Byte(memory[dest.toInt()+6]),
                DataType.Byte(memory[dest.toInt()+7])
            )
        )
    )
}

@ExperimentalStdlibApi
class VM(val binary: Executable){
    val memory = Memory()
    val stack = Stack()

    private fun reference(): DataType {
        val next = binary.next()
        val nextInstr = InstructionSet.values().find { it.code == next }
        return when{
            nextInstr != null -> {
                when(nextInstr){
                    InstructionSet.BYTE -> memory.readByte(binary.next())
                    InstructionSet.WORD -> memory.readWord(binary.next())
                    InstructionSet.DWORD -> memory.readDoubleWord(binary.next())
                    InstructionSet.QWORD -> memory.readQuadWord(binary.next())
                    else -> memory.readByte(next)
                }
            }
            else -> memory.readByte(next)
        }
    }

    private fun move(){
        val dest = binary.next()
        val destInstr = InstructionSet.values().find { it.code == dest }
        val destVal = if(destInstr != null){
            when(destInstr){
                InstructionSet.REF -> {
                    reference()
                }
                else -> DataType.Byte(dest)
            }
        }else{
            DataType.Byte(dest)
        }
        val target = binary.next()
        val targetInstr = InstructionSet.values().find { it.code == target }
        val targetVal = if(targetInstr != null){
            when(targetInstr){
                InstructionSet.REF -> {
                    reference()
                }
                InstructionSet.TOP -> DataType.Byte(stack.top)
                else -> DataType.Byte(target)
            }
        }else{
            DataType.Byte(target)
        }
        when{
            targetInstr == null && destInstr == null -> memory.writeByte(dest, memory.readByte(target))
            targetInstr != null && destInstr == null ->
                when(targetInstr){
                    InstructionSet.TOP -> memory.writeByte(dest, DataType.Byte(InstructionSet.TOP.code))
                    InstructionSet.REF -> {
                        when(val ref = reference()){
                            is DataType.Byte -> memory.writeByte(dest, ref)
                            is DataType.Word -> memory.writeWord(dest, ref)
                            is DataType.DoubleWord -> memory.writeDouble(dest, ref)
                            is DataType.QuadWord -> memory.writeQuad(dest, ref)
                        }
                    }
                    InstructionSet.BYTE -> memory.writeByte(dest, memory.readByte(target))
                    InstructionSet.WORD -> memory.writeByte(dest, memory.readWord(target).data2)
                    InstructionSet.DWORD -> memory.writeByte(dest, memory.readDoubleWord(target).data2.data2)
                    InstructionSet.QWORD -> memory.writeByte(dest, memory.readQuadWord(target).data2.data2.data2)
                    else -> {
                        println("Unknown memory address: $targetInstr")
                        return
                    }
                }
            targetInstr == null && destInstr != null ->
                when(targetInstr){
                    InstructionSet.REF -> {
                        when(val ref = reference()){
                            is DataType.Byte -> memory.writeByte(dest, ref)
                            is DataType.Word -> memory.writeWord(dest, ref)
                            is DataType.DoubleWord -> memory.writeDouble(dest, ref)
                            is DataType.QuadWord -> memory.writeQuad(dest, ref)
                        }
                    }
                    InstructionSet.BYTE -> memory.writeByte(dest, memory.readByte(target))
                    InstructionSet.WORD -> memory.writeWord(dest, DataType.Word(DataType.Byte(0x0), memory.readByte(target)))
                    InstructionSet.DWORD -> memory.writeDouble(dest,
                        DataType.DoubleWord(
                            DataType.Word(
                                DataType.Byte(0x0),
                                memory.readByte(target)
                            ),
                            DataType.Word(
                                DataType.Byte(0x0),
                                memory.readByte(target)
                            )
                        )
                    )
                    InstructionSet.QWORD -> memory.writeQuad(dest,
                        DataType.QuadWord(
                            DataType.DoubleWord(
                                DataType.Word(
                                    DataType.Byte(0x0),
                                    memory.readByte(target)
                                ),
                                DataType.Word(
                                    DataType.Byte(0x0),
                                    memory.readByte(target)
                                )
                            ),
                            DataType.DoubleWord(
                                DataType.Word(
                                    DataType.Byte(0x0),
                                    memory.readByte(target)
                                ),
                                DataType.Word(
                                    DataType.Byte(0x0),
                                    memory.readByte(target)
                                )
                            )
                        )
                    )
                    else -> {
                        println("Unknown memory address: $destInstr")
                        return
                    }
                }
        }
    }

    private fun moveByte(){
        val dest = binary.next()
        val destInstr = InstructionSet.values().find { it.code == dest }
        val target = binary.next()
        val targetInstr = InstructionSet.values().find { it.code == target }
        when{
            targetInstr == null && destInstr == null -> memory.writeByte(dest, DataType.Byte(target))
            targetInstr != null && destInstr == null ->
                when(targetInstr) {
                    InstructionSet.TOP -> memory.writeByte(dest, DataType.Byte(InstructionSet.TOP.code))
                    else -> {
                        println("Unknown memory address: $targetInstr")
                        return
                    }
                }
        }
    }

    private fun moveWord(){
        val dest = binary.next()
        val destInstr = InstructionSet.values().find { it.code == dest }
        val target = binary.nextWord()
        val targetInstr = InstructionSet.values().find { it.code == target.data1.data }
        when{
            targetInstr == null && destInstr == null -> {
                memory.writeWord(dest, target)
            }
            targetInstr != null && destInstr == null ->
                when(targetInstr){
                    InstructionSet.TOP -> {
                        memory.writeByte(dest, DataType.Byte(InstructionSet.TOP.code))
                    }
                    else -> {
                        println("Unknown memory address: $targetInstr")
                        return
                    }
                }
        }
    }

    private fun moveDoubleWord(){
        val dest = binary.next()
        val destInstr = InstructionSet.values().find { it.code == dest }
        val target = binary.nextDoubleWord()
        val targetInstr = InstructionSet.values().find { it.code == target.data1.data1.data }
        when{
            targetInstr == null && destInstr == null -> {
                memory.writeDouble(dest, target)
            }
            targetInstr != null && destInstr == null ->
                when(targetInstr){
                    InstructionSet.TOP -> {
                        memory.writeByte(dest, DataType.Byte(InstructionSet.TOP.code))
                    }
                    else -> {
                        println("Unknown memory address: $targetInstr")
                        return
                    }
                }
        }
    }

    private fun moveQuadWord(){
        val dest = binary.next()
        val destInstr = InstructionSet.values().find { it.code == dest }
        val target = binary.nextQuadWord()
        val targetInstr = InstructionSet.values().find { it.code == target.data1.data1.data1.data }
        when{
            targetInstr == null && destInstr == null -> {
                memory.writeQuad(dest, target)
            }
            targetInstr != null && destInstr == null ->
                when(targetInstr){
                    InstructionSet.TOP -> {
                        memory.writeByte(dest, DataType.Byte(InstructionSet.TOP.code))
                    }
                    else -> {
                        println("Unknown memory address: $targetInstr")
                        return
                    }
                }
        }
    }

    private fun push(){
        val data = binary.nextByte()
        stack.push(data)
    }

    private fun pop(){
        stack.pop()
    }

    private fun jump(){
        val location = binary.next()
        val locInstr = InstructionSet.values().find { it.code == location }
        if(locInstr == null){
            binary.instructionPtr = location.toInt()
        }
        when(locInstr){
            InstructionSet.TOP -> binary.instructionPtr = stack.top.toInt()
        }
    }

    private fun add(){
        val left = binary.nextByte()
        val leftInstr = InstructionSet.values().find { it.code == left.data }
        val leftValue = if(leftInstr != null){
            when(leftInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val addr = binary.nextByte()
                    memory.readByte(addr.data)
                }
                else -> left
            }
        }else{
            left
        }
        val right = binary.nextByte()
        val rightInstr = InstructionSet.values().find { it.code == right.data }
        val rightValue = if(leftInstr != null){
            when(rightInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val addr = binary.nextByte()
                    memory.readByte(addr.data)
                }
                else -> right
            }
        }else{
            right
        }
        val sum = leftValue + rightValue
        stack.push(sum)
    }

    private fun sub(){
        val left = binary.nextByte()
        val leftInstr = InstructionSet.values().find { it.code == left.data }
        val leftValue = if(leftInstr != null){
            when(leftInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val addr = binary.nextByte()
                    memory.readByte(addr.data)
                }
                else -> left
            }
        }else{
            left
        }
        val right = binary.nextByte()
        val rightInstr = InstructionSet.values().find { it.code == right.data }
        val rightValue = if(leftInstr != null){
            when(rightInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val addr = binary.nextByte()
                    memory.readByte(addr.data)
                }
                else -> right
            }
        }else{
            right
        }

        val difference = leftValue - rightValue
        stack.push(difference)
    }

    private fun mul(){
        val left = binary.nextByte()
        val leftInstr = InstructionSet.values().find { it.code == left.data }
        val leftValue = if(leftInstr != null){
            when(leftInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val addr = binary.nextByte()
                    memory.readByte(addr.data)
                }
                else -> left
            }
        }else{
            left
        }
        val right = binary.nextByte()
        val rightInstr = InstructionSet.values().find { it.code == right.data }
        val rightValue = if(leftInstr != null){
            when(rightInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val addr = binary.nextByte()
                    memory.readByte(addr.data)
                }
                else -> right
            }
        }else{
            right
        }

        val product = leftValue * rightValue
        stack.push(product)
    }

    private fun div(){
        val left = binary.nextByte()
        val leftInstr = InstructionSet.values().find { it.code == left.data }
        val leftValue = if(leftInstr != null){
            when(leftInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val addr = binary.nextByte()
                    memory.readByte(addr.data)
                }
                else -> left
            }
        }else{
            left
        }
        val right = binary.nextByte()
        val rightInstr = InstructionSet.values().find { it.code == right.data }
        val rightValue = if(leftInstr != null){
            when(rightInstr){
                InstructionSet.TOP -> DataType.Byte(stack.top)
                InstructionSet.REF -> {
                    val addr = binary.nextByte()
                    memory.readByte(addr.data)
                }
                else -> right
            }
        }else{
            right
        }

        val quotient = leftValue / rightValue
        stack.push(quotient)
    }

    fun run(){
        while(binary.hasNext()){
            val next = binary.next()
            val instr = InstructionSet.values().find { it.code == next }
            if(instr == null){
                println("Invalid opcode: $next")
                return
            }
            when(instr){
                InstructionSet.MOVE -> move()
                InstructionSet.MOVB -> moveByte()
                InstructionSet.MOVW -> moveWord()
                InstructionSet.MOVD -> moveDoubleWord()
                InstructionSet.MOVL -> moveQuadWord()
                InstructionSet.PUSH -> push()
                InstructionSet.POP -> pop()
                InstructionSet.JMP -> jump()
                InstructionSet.ADD -> add()
                InstructionSet.SUB -> sub()
                InstructionSet.MUL -> mul()
                InstructionSet.DIV -> div()
            }
        }
    }
}

@ExperimentalStdlibApi
fun main(){
    val file = File("asm/test.lasm")
    if(!file.exists()){
        println("asm/test.lasm doesn't exist!")
        return
    }
    val asm = ASMLoader(file)
    val exec = when(val result = asm.executable){
        is Some -> result.t
        is None -> return
    }
    println(exec)
//    val exec = Executable(byteArrayOf(
//        InstructionSet.ADD.code,
//        5,
//        3,
//        InstructionSet.MOVL.code,
//        0,
//        InstructionSet.TOP.code,
//        InstructionSet.POP.code,
//        InstructionSet.DIV.code,
//        InstructionSet.REF.code,
//        0,
//        2,
//        InstructionSet.MOVL.code,
//        1,
//        InstructionSet.TOP.code,
//        InstructionSet.POP.code
//    )
//    )
    val vm = VM(exec)
    vm.run()
}