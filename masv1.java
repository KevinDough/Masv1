import java.io.*;
class masv1
{
    private static String fileName;
    public static void main(String[] args) throws IOException
    {
        SymbolTable symTable = new SymbolTable(20);
        short location_counter = 0;
        String buffer = "";
        String inFileName = "";
        String outFileName = "";
        BufferedReader inStream = null;
        DataOutputStream outStream = null;

        System.out.println("masv1 written by Kevin DohDerpy");

        try
        {
            if (!args[0].matches("([^\\s]+(\\.(?i)(...|..|.))$)"))
            {
                inFileName = args[0] + ".mas";
                outFileName = args[0] + ".mac";
            }
            else
            {
                inFileName = args[0];
                outFileName = args[0].substring(0, args[0].lastIndexOf(".")) + ".mac";
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            System.out.println("Incorrect number of command line arguments");
            System.exit(1);
        }
        fileName = inFileName;

        // pass 1 ======================================================================
        try
        {
            inStream = new BufferedReader(new FileReader(inFileName));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Cannot open input file " + inFileName);
            System.exit(1);
        }

        try
        {
            outStream = new DataOutputStream(new FileOutputStream(outFileName));
        }
        catch (IOException e)
        {
            System.out.println("Cannot open output file " + outFileName);
            System.exit(1);
        }

        buffer = inStream.readLine();
        while (buffer != null)
        {
            String operand = null;
            String label = null;
            if (buffer.trim().length() == 0)
                buffer = inStream.readLine();

            if (buffer.contains(":"))
            {
                String restOfLine = null;
                int i = buffer.indexOf(":");
                label = buffer.substring(0, i);                 // isolate the label
                label = label.trim();
                int symTableError = 0;
                if (Character.isDigit(label.charAt(0)) || !label.matches("[a-zA-Z0-9_@$]+"))    // label starts with a digit or has illegal chars
                    printError(location_counter, buffer, 2);    // Ill-formed label in label field
                else
                {
                    symTableError = symTable.enter(label, location_counter);
                    if (symTableError == 1)                     // Duplicate label
                        printError(location_counter, buffer, 5);
                    if (symTableError == 2)                     // Symbol table overflow
                        printError(location_counter, buffer, 7);
                }
                restOfLine = buffer.substring(i + 1);           // isolate the rest of the line
                restOfLine = restOfLine.trim();
                String[] tokenize = restOfLine.split("\\s+");   // split operand and mnemonic
                if (tokenize.length == 1)
                    operand = "0";                              // no operand
                else
                    operand = tokenize[1];
            }
            else
            {
                buffer = buffer.trim();
                String[] tokenize = buffer.split("\\s+");       // split operand and mnemonic
                if (tokenize.length == 1)
                    operand = "0";                              // no operand
                else
                    operand = tokenize[1];
            }
            try
            {
                Integer.parseInt(operand);
            }
            catch(NumberFormatException e)                      // if operand is NOT numeric, it is a label
            {
                if (Character.isDigit(operand.charAt(0)) || !operand.matches("[a-zA-Z0-9_@$]+"))
                    printError(location_counter, buffer, 3);    // Ill-formed label in operand field
                else
                {
                    outStream.writeByte('R');
                    outStream.writeShort(reverseOrder(location_counter));
                }
            }
            location_counter++;
            buffer = inStream.readLine();
            if (location_counter > 4095)
                printError(location_counter, buffer, 8);        // Program too big
        }
        outStream.writeByte('T');
        inStream.close();
        location_counter = 0;

        // pass 2 ======================================================================
        try
        {
            inStream = new BufferedReader(new FileReader(inFileName));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("ERROR: Cannot open input file " + inFileName);
            System.exit(1);
        }

        buffer = inStream.readLine();
        while (buffer != null)
        {
            String operation = null;
            String operand = null;
            String label = null;
            if (buffer.trim().length() == 0)
                buffer = inStream.readLine();
            if (buffer.contains(":"))
            {
                String restOfLine = null;
                int i = buffer.indexOf(":");
                label = buffer.substring(0, i);                 // isolate the label
                label = label.trim();

                restOfLine = buffer.substring(i + 1);           // isolate the rest of the line
                restOfLine = restOfLine.trim();
                String[] tokenize = restOfLine.split("\\s+");   // split operand and mnemonic
                if (tokenize.length == 1)
                {
                    operation = tokenize[0];
                    operand = "0";                              // no operand                          
                }
                else
                {    
                    operation = tokenize[0];
                    operand = tokenize[1];
                }
            }
            else
            {
                buffer = buffer.trim();
                String[] tokenize = buffer.split("\\s+");       // split operand and mnemonic
                if (tokenize.length == 1)
                {    
                    operation = tokenize[0];
                    operand = "0";                              // no operand
                }
                else
                {
                    operation = tokenize[0];
                    operand = tokenize[1];
                }
            }
            try                                                 // if operand is numeric, NOT a label
            {
                int tempOperand = Integer.parseInt(operand);
                if (operation.equals("dw")) {
                    if (tempOperand < -32768 || tempOperand > 65535)
                        printError(location_counter, buffer, 6);    // Address or operand out of range
                }
                else if (operation.equals("aloc") || operation.equals("dloc")) {
                    if (tempOperand < 0 || tempOperand > 255)
                        printError(location_counter, buffer, 6);    // Address or operand out of range
                }
                else if (tempOperand < 0 || tempOperand > 4095)
                    printError(location_counter, buffer, 6);        // Address or operand out of range      
            }
            catch(NumberFormatException e)                      // operand is a label
            {
                if (symTable.search(operand) == -1)
                    printError(location_counter, buffer, 4);    // Undefined label in operand field
            }
            Assembler as = new Assembler();
            if (as.assemble(operation, operand, symTable) == 1)
                printError(location_counter, buffer, 1);        // Invalid operation
            outStream.writeShort(as.getMachine_word());
            buffer = inStream.readLine();
            location_counter++;
        }
        inStream.close();
        outStream.close();
    }

    public static void printError(short lineNum, String line, int code)
    {   
        String error = "";
        switch(code) {
            case 1:     error = "Invalid operation";                 break;
            case 2:     error = "Ill-formed label in label field";   break;
            case 3:     error = "Ill-formed label in operand field"; break;
            case 4:     error = "Undefined label in operand field";  break;
            case 5:     error = "Duplicate label";                   break;
            case 6:     error = "Address or operand out of range";   break;
            case 7:     error = "Symbol table overflow";             break;
            case 8:     error = "Program too big";                   break;
        }
        System.out.println("ERROR on line " + ++lineNum + " of " + fileName + ":");
        System.out.println(line);
        System.out.println(error);
        System.exit(1);
    }

    public static short reverseOrder(short x)
    {
        int y = ((int)x) & 0xffff;                          //promote with no sign ext
        return (short) (256 * (y % 256) + y / 256);
    }
}