import java.util.HashMap;
class Assembler
{
    private int opcode;
    private int operand_value;
    private short machine_word;
    
    public short getMachine_word()
    {
        return machine_word;
    }
    public short reverseOrder(short x)
    {
        int y = ((int)x) & 0xffff;                          //promote with no sign ext
        return (short) (256 * (y % 256) + y / 256);
    }
    public short assemble(String operation, String operand, SymbolTable symTable)
    {
        // Assemble machine word from mnemonic and operand using
        // the symbol and opcode tables.

        HashMap<String, Integer> opcode_table = new HashMap<String, Integer>(47, 0.75f);
        opcode_table.put("ld", 0x0000);
        opcode_table.put("st", 0x0001);
        opcode_table.put("add", 0x0002);
        opcode_table.put("sub", 0x0003);
        opcode_table.put("ldr", 0x0004);
        opcode_table.put("str", 0x0005);
        opcode_table.put("addr", 0x0006);
        opcode_table.put("subr", 0x0007);
        opcode_table.put("ldc", 0x0008);
        opcode_table.put("ja", 0x0009);
        opcode_table.put("jzop", 0x000A);
        opcode_table.put("jn", 0x000B);
        opcode_table.put("jz", 0x000C);
        opcode_table.put("jnz", 0x000D);
        opcode_table.put("call", 0x000E);
        opcode_table.put("ret", 0x00F0);
        opcode_table.put("ldi", 0x00F1);
        opcode_table.put("sti", 0x00F2);
        opcode_table.put("push", 0x00F3);
        opcode_table.put("pop", 0x00F4);
        opcode_table.put("aloc", 0x00F5);
        opcode_table.put("dloc", 0x00F6);
        opcode_table.put("swap", 0x00F7);
        opcode_table.put("uout", 0xFFF5);
        opcode_table.put("sin", 0xFFF6);
        opcode_table.put("sout", 0xFFF7);
        opcode_table.put("hin", 0xFFF8);
        opcode_table.put("hout", 0xFFF9);
        opcode_table.put("ain", 0xFFFA);
        opcode_table.put("aout", 0xFFFB);
        opcode_table.put("din", 0xFFFC);
        opcode_table.put("dout", 0xFFFD);
        opcode_table.put("bkpt", 0xFFFE);
        opcode_table.put("halt", 0xFFFF);

        try
        {
            operand_value = Integer.parseInt(operand);         // operand is a number
        }
        catch (NumberFormatException e)
        {
            operand_value = symTable.search(operand);          // operand is a label
        }
        if (opcode_table.containsKey(operation))               // if operation is a mnemonic
        {
            opcode = opcode_table.get(operation);
            if (opcode <= 0x000E)
                machine_word = (short)((opcode << 12) | operand_value);     // assemble opcode and operand_value
            else if (opcode >= 0x00F0 && opcode <= 0x00F7)
                machine_word = (short)((opcode << 8) | operand_value);
            else if (opcode >= 0xFFF5)
                machine_word = (short)opcode;
            machine_word = reverseOrder(machine_word);
        }
        else if (operation.equals("dw"))                       // operation is a dw
            {
                machine_word = (short)operand_value;
                machine_word = reverseOrder(machine_word);
            }
        else
        {
            return 1;                    // operation was not a mnemonic or a dw
        }
        return 0;                        // no errors
    }
}