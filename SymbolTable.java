class SymbolTable
{
    private String symbol[];         // label array
    private short addressArray[];    // address array
    private int index;               // index of next available slot
    private int symCount;            // number of items in symbol table
    
    public SymbolTable(int size)
    {
        symbol = new String[size];
        addressArray = new short[size];
        index = 0;
        symCount = 0;
    }

    public short enter(String label, short address)
    {
        if (search(label) == -1)        // label is not already in symTable
        {
            try
            {    
                symbol[index] = label;
                addressArray[index] = address;
                index++;
                symCount++;
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                return 2;                   // Symbol table overflow
            }
            return 0;                       // no errors
        }
        else
        {
            return 1;       // Duplicate label
        }
    }

    public short search(String label)
    {
        for (short i = 0; i < symCount; i++)
        {
            if (symbol[i].equals(label))
                return addressArray[i];
        }
        return -1;
    }
}