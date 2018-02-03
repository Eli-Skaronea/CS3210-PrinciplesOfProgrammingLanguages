import java.io.*;
import java.util.*;

public class Main {
    //Todo warnings for labels - check with label list
    //Todo outputLog file - fix format and file name
    //Todo fix pathing question
    //Todo count number of each error
    //Todo Cover letter
    //Todo return all error messages -EXTRA CREDIT
    //Todo refactor



    private static BufferedWriter logFile = null;
    private static BufferedReader reader = null;
    public static Map<String, operandRules> opcodeMap = buildOpcodesMap();
    public static ArrayList<Integer> operandListByType = new ArrayList<>();
    public static ArrayList<String> labelList = new ArrayList<>();
    public static ArrayList<String> labelsCalledList = new ArrayList<>();
    public static ArrayList<Integer> errorCountByError = new ArrayList<>();
    public static String errorMessage = "";


    public static void main(String[] args) throws IOException {

        //String fileGiven = args[0].toString();
        //String malFile = fileGiven + ".mal";
        //fileGiven = fileGiven + ".txt";

        String fileGiven = "C:\\Users\\Eli S\\IdeaProjects\\CS3210__Project1_LogPrinter\\src\\test.txt";


        openFile(fileGiven);
        readFile(reader, logFile);


        logFile.close();
        reader.close();

    }

    /**
     * Opens the file passed into the program arguments
     *
     * @param fileName - name of the .mal file
     */
    private static void openFile(String fileName) {
        try {
            FileReader fileBeingChecked = new FileReader(fileName);
            reader = new BufferedReader(fileBeingChecked);
        } catch (Exception e) {
            System.out.println("Could not find correct file");
        }
        try {
            FileWriter outFile = new FileWriter("Log Output.txt");
            logFile = new BufferedWriter(outFile);
        } catch (IOException o) {
            System.out.println("Problem creating output file!");
            o.printStackTrace();
        }
    }


    /**
     * Reads the file line by line, removes comments, then checks for errors
     * Each line is places in an array of Characters then analyzed Character by Character
     *
     * @param currentFile - file currently being read and checked for errors
     * @param logFile     - output log file removing all comments and giving error messages
     * @throws IOException
     */
    private static void readFile(BufferedReader currentFile, BufferedWriter logFile) throws IOException {
        String currentLine = null;
        int numberOfErrors = 0;
        int lineNumber = 1;
        ArrayList<Character> lettersInCurrentLine = new ArrayList<>();

        while ((currentLine = currentFile.readLine()) != null) {
            errorMessage = "";
            ArrayList<String> currentLineWordByWord = new ArrayList<>();
            lettersInCurrentLine.clear();

            if (currentLine.contains(";")) {
                currentLine = removeCommentFromLine(currentLine);
            }

            currentLine = currentLine.trim();


            if (!currentLine.isEmpty()) {
                operandListByType.clear();
                currentLineWordByWord = splitCurrentLine(currentLine);
                errorMessage = runErrorCheck(currentLineWordByWord, currentLine);

                logFile.write(lineNumber + " " + currentLine + '\n');
                System.out.println(lineNumber + " " + currentLine);
                lineNumber++;
            }
            //Write the line to the output file if errorMessage contains characters, output them
            if (!errorMessage.isEmpty()) {
                logFile.write(errorMessage + '\n');
                System.out.println(errorMessage);
                numberOfErrors++;
            }
        }
        logFile.write('\n');
        outputWarnings();
        outputErrorReport(numberOfErrors);
    }

    /**
     * My personal split method to split into an array of strings
     * @param currentLine - the current line be read by reader
     * @return - The current line word by word
     */
    private static ArrayList<String> splitCurrentLine(String currentLine) {
        int start = 0;
        int end;
        int counter = 0;
        ArrayList<String> currentLineWordByWord = new ArrayList<>();
        for (int i = 0; i < currentLine.length(); i++) {
            Character letter = currentLine.charAt(i);
            if (letter.equals(' ')) {
                end = i;
                if(!currentLine.substring(start, end).equals(" ")) {
                    currentLineWordByWord.add(currentLine.substring(start, end));
                    counter++;
                    start = end;
                } else{
                    start = i;
                }
            }

        }

        currentLineWordByWord.add(currentLine.substring(start, currentLine.length()));
        for (int i = 0; i < counter; i++) {
            currentLineWordByWord.get(i).trim();
        }

        return currentLineWordByWord;

    }

    //Todo number of each error
    private static void outputErrorReport(int numberOfErrors) throws IOException {
        logFile.write("------------" + '\n');
        logFile.write(numberOfErrors + " Errors found" + '\n');
        logFile.write("Processing complete – ");
        if(numberOfErrors != 0){
            logFile.write("MAL program is not valid");
        }
        else{
            logFile.write("MAL program is valid");
        }
    }


    //Todo test method
    private static void outputWarnings() {
        for(int i = 0; i < labelsCalledList.size(); i++){
            String calledLabel = labelsCalledList.get(i);
            if(!labelList.contains(calledLabel)){
                System.out.println("WARNING: " + calledLabel + " called and not defined or defined and never used");
                labelList.add(calledLabel);
            }
        }
    }



    private static String runErrorCheck(ArrayList<String> currentLineWordByWord, String currentLine) throws IOException {


        boolean hasLabel = checkForLabel(currentLineWordByWord.get(0));
        boolean validLabel = validLabelOpcode(currentLineWordByWord.get(0));

        if (hasLabel == true) {
            updateLabelList(currentLineWordByWord.get(0));
            if (!validLabel) {
                errorMessage = "Invalid label";
                return errorMessage;
            }
            currentLineWordByWord = removeLabel(currentLineWordByWord, 0);
            StringBuilder currentLineBuilder = new StringBuilder();
            for(int i = 0; i <currentLineWordByWord.size(); i++){
                currentLineBuilder.append(currentLineWordByWord.get(i));
            }
            currentLine = currentLineBuilder.toString();
        }

        if (!currentLineWordByWord.isEmpty()) {
            errorMessage = checkIfValidOpcode(currentLineWordByWord, currentLine);

        }
        return errorMessage;
    }



    /**
     * Checks if the opcode is valid - cannot start with a number
     *
     * @param currentLineWordByWord
     * @return
     */
    private static String checkIfValidOpcode(ArrayList<String> currentLineWordByWord, String currentLine) throws IOException {
        ArrayList<String> listOfOperands;
        listOfOperands = new ArrayList<>();
        String opcode = currentLineWordByWord.get(0).toUpperCase();
        opcode = opcode.trim();
        errorMessage = validateOpcode(opcode);
        if(!errorMessage.isEmpty()){
            return errorMessage;
        }

        listOfOperands = buildListOfOperands(currentLine, opcode);

        if (operandListByType.size() > 3) {
            errorMessage = "Max operands is 3";
            return errorMessage;
        }

        if (!listOfOperands.get(0).equals("")) {
            errorMessage = validateOperands(listOfOperands, opcode);
        }

        if (errorMessage.isEmpty()) {
            errorMessage = validateOperandsMatchOpcodes(opcode);
        }
        return errorMessage;
    }

    private static String validateOperandsMatchOpcodes(String opcode) throws IOException {
        operandRules opcodeRules = opcodeMap.get(opcode);
//        if (opcode.equals("END") && !operandListByType.isEmpty()) {
//            errorMessage = "END Opcode has no operands";
//            return errorMessage;
//        } else if (opcode.equals("END")) {
//            boolean endOfProgram = endOfProgram();
//            if (endOfProgram) {
//                return errorMessage;
//            } else {
//                errorMessage = "END must be the last opcode given in the program";
//                return errorMessage;
//            }
//        }
        if (opcodeRules.operandTypes.size() != operandListByType.size()) {
            errorMessage = "Incorrect amount of operands for opcode: " + opcode +
                    " expects " + opcodeRules.operandTypes.size() + " operands.";
            return errorMessage;
        }
        for (int i = 0; i < operandListByType.size(); i++) {
            if (!opcodeRules.operandTypes.get(i).contains(operandListByType.get(i))) {
                errorMessage = "Incorrect operand for opcode";
                return errorMessage;
            }
        }
        return errorMessage;
    }

    private static ArrayList<String> buildListOfOperands(String currentLine, String opcode) {
        ArrayList<Character> currentLineLetterByLetter = new ArrayList<>();
        ArrayList<String> listOfOperands = new ArrayList<>();
        for (int i = opcode.length(); i < currentLine.length(); i++) {
            currentLineLetterByLetter.add(currentLine.charAt(i));
        }
        ArrayList<Character> operandLetters = new ArrayList<>();
        for (Character character : currentLineLetterByLetter) {
            operandLetters.add(character);
            if (character.equals(',')) {
                listOfOperands.add(buildString(operandLetters));
                operandLetters.clear();
            }
        }

        listOfOperands.add(buildString(operandLetters));
        operandLetters.clear();

        return listOfOperands;
    }

    private static String validateOpcode(String opcode) {
        if (!opcodeMap.containsKey(opcode)) {
            errorMessage = "Invalid opcode: " + opcode + " is not a valid opcode";
            return errorMessage;
        }

        if (opcode.charAt(0) < 65 || opcode.charAt(0) > 122) {
            errorMessage = "Opcode cannot start with a number";
            return errorMessage;
        }

        return errorMessage;
    }

    //Todo Gurka test 3 crashes on end when some spaces added at end?
    private static boolean endOfProgram() throws IOException {
        boolean end = true;
//        while ((reader.readLine()) != null) {
//            if (!reader.readLine().isEmpty()) {
//                end = false;
//                return end;
//            }
//        }

        return end;
    }

    /**
     * Validate operands are not illegal instructions
     *
     * @param listOfOperands - A list of strings each an operand
     * @return - error message if one found, otherwise returns an empty string.
     */
    private static String validateOperands(ArrayList<String> listOfOperands, String opcode) {

        //run through the list of operands and check if it's legal
        for (int i = 0; i < listOfOperands.size(); i++) {
            String operand = listOfOperands.get(i);
            operand = operand.trim();

            //If the operand starts with a number it has to be an immediate value - validate immediate value
            if (operand.charAt(0) > 47 && operand.charAt(0) < 58) {
                errorMessage = validateImmediateValueOperand(operand);
                if (!errorMessage.isEmpty()) {
                    return errorMessage;
                } else {
                    updateListOfOperands(2);
                }
            }

            else if (opcode.startsWith("B")) {
                if (opcode.equals("BR") || i == 2) {
                    validateCalledLabel(operand);

                } else if (operand.startsWith("R") && Character.isDigit(operand.charAt(1))) {
                    errorMessage = validateRegisterOperand(operand);
                    if (!errorMessage.isEmpty()) {
                        return errorMessage;
                    } else {
                        updateListOfOperands(1);
                    }
                } else {
                    errorMessage = validateMemoryOperand(operand);
                    if (!errorMessage.isEmpty()) {
                        return errorMessage;
                    } else {
                        updateListOfOperands(0);
                    }
                }

            }

            //If operand starts with an 'R' and second character is a number may be a register
            else if (operand.startsWith("R") && Character.isDigit(operand.charAt(1))) {
                errorMessage = validateRegisterOperand(operand);
                if (!errorMessage.isEmpty()) {
                    return errorMessage;
                } else {
                    updateListOfOperands(1);
                }
            }

            //Memory location - has to be all letters and less than 5 characters, could still start with an 'R'
            else if (operand.length() < 7) {
                if (operand.endsWith(",")) {
                    operand = operand.substring(0, operand.length() - 1);
                }
//                if (opcode.startsWith("B")) {
//                    if (opcode.equals("BR")) {
//                        errorMessage = validateMemoryOperand(operand);
//                        if (!errorMessage.isEmpty()) {
//                            return errorMessage;
//                        } else {
//                            updateListOfOperands(3);
//                        }
//                    } else if (i == 3) {
//                        errorMessage = validateMemoryOperand(operand);
//                        if (!errorMessage.isEmpty()) {
//                            return errorMessage;
//                        } else {
//                            updateListOfOperands(3);
//                        }
//                    }
//                }
                if (operand.charAt(0) < 65 && operand.charAt(0) > 122) {
                    errorMessage = "Invalid Operand " + i + 1 + ": Operand must start with a letter";
                    return errorMessage;
                }
                errorMessage = validateMemoryOperand(operand);
                if (!errorMessage.isEmpty()) {
                    return errorMessage;
                } else {
                    updateListOfOperands(0);
                }
            } else {
                errorMessage = "Operand " + i + " is to long. Memory locations can only be 5 characters long. Check for " +
                        "missing comma";
                return errorMessage;
            }

        }
        return errorMessage;
    }

    private static String validateCalledLabel(String operand) {
        errorMessage = validateMemoryOperand(operand);
        if (!errorMessage.isEmpty()) {
            errorMessage = "The third operand must be a label (valid memory location)";
            return errorMessage;
        } else {
            updateListOfLabelsCalled(operand + ":");
            updateListOfOperands(3);

        }
        return errorMessage;
    }

    private static void updateListOfLabelsCalled(String label) {
        labelsCalledList.add(label);
    }

    /**
     * Validates an immediate value operand - must be all digits and in octal
     *
     * @param operand - operand being checked
     * @return - error message if one found
     */
    private static String validateImmediateValueOperand(String operand) {
        if (operand.endsWith(",")) {
            operand = operand.substring(0, operand.length() - 1);
        }
        for (int i = 0; i < operand.length(); i++) {
            char digit = operand.charAt(i);
            if (!Character.isDigit(digit)) {
                errorMessage = "Immediate value must be all numbers";
                return errorMessage;
            } else {
                if (digit > 57 || digit < 47) {
                    errorMessage = "Immediate value must be in octal (0-8)";
                    return errorMessage;
                }
            }
        }
        return errorMessage;
    }

    private static String validateRegisterOperand(String operand) {
        if (operand.length() == 2 && (operand.charAt(1) < 48 || operand.charAt(1) > 55)) {
            errorMessage = "Invalid register number (0-7)";
            return errorMessage;
        }

        return errorMessage;
    }

    private static String validateMemoryOperand(String operand) {
        for (int i = 0; i < operand.length(); i++) {
            char letter = operand.charAt(i);
            if (Character.isDigit(letter)) {
                errorMessage = "Memory operand cannot contain numbers";
                return errorMessage;
            }
        }

        return errorMessage;
    }

    private static void updateListOfOperands(int operandCode) {
        operandListByType.add(operandCode);
    }

    private static void clearListOfOperands() {
        operandListByType.clear();
    }


    /**
     * Removes comments from current line being read, is called if a ';' is found anywhere in a line of code
     *
     * @param currentLine - current line
     */
    private static String removeCommentFromLine(String currentLine) {
        ArrayList<Character> lettersInCurrentLine = new ArrayList<>();
        for (int i = 0; i < currentLine.length(); i++) {
            lettersInCurrentLine.add(currentLine.charAt(i));
        }
        int indexWhereCommentBegins = 0;

        for (int i = 0; i < lettersInCurrentLine.size(); i++) {
            if (lettersInCurrentLine.get(i) == ';') {
                indexWhereCommentBegins = i;
                break;
                }
            }
        lettersInCurrentLine.subList(indexWhereCommentBegins, lettersInCurrentLine.size()).clear();
        return buildString(lettersInCurrentLine);
    }

    private static String buildString(ArrayList<Character> listOfCharacters) {
        StringBuilder result = new StringBuilder(listOfCharacters.size());
        for (Character c : listOfCharacters) {
            result.append(c);
        }
        String builtString = result.toString();
        return builtString;
    }


    public static class operandRules {
        ArrayList<ArrayList<Integer>> operandTypes;

        public operandRules(ArrayList<ArrayList<Integer>> validOperandTypes) {
            operandTypes = validOperandTypes;
        }
    }

    /**
     * Creates the dictionary to check if an opcode is valid - opcode must match the key name, and each operand must
     * be contained in the array operandRules
     *
     * @return
     */
    private static Map<String, operandRules> buildOpcodesMap() {
        ArrayList<Integer> reg_mem = createOperandTypeArrays("reg_mem");
        ArrayList<Integer> reg_mem_immediate = createOperandTypeArrays("reg_mem_immediate");
        ArrayList<Integer> label = createOperandTypeArrays("label");

        ArrayList<ArrayList<Integer>> moveOperandTypes = new ArrayList<>();
        moveOperandTypes.add(reg_mem);
        moveOperandTypes.add(reg_mem);

        ArrayList<ArrayList<Integer>> moveImmediateOperandTypes = new ArrayList<>();
        moveImmediateOperandTypes.add(reg_mem_immediate);
        moveImmediateOperandTypes.add(reg_mem);

        ArrayList<ArrayList<Integer>> mathOperandTypes = new ArrayList<>();
        mathOperandTypes.add(reg_mem_immediate);
        mathOperandTypes.add(reg_mem_immediate);
        mathOperandTypes.add(reg_mem);

        ArrayList<ArrayList<Integer>> incDecOperandTypes = new ArrayList<>();
        incDecOperandTypes.add(reg_mem);

        ArrayList<ArrayList<Integer>> conditionalBranchOperandTypes = new ArrayList<>();
        conditionalBranchOperandTypes.add(reg_mem_immediate);
        conditionalBranchOperandTypes.add(reg_mem_immediate);
        conditionalBranchOperandTypes.add(label);


        ArrayList<ArrayList<Integer>> unconditionalBranchOperandTypes = new ArrayList<>();
        unconditionalBranchOperandTypes.add(label);

        ArrayList<ArrayList<Integer>> endOperandTypes = new ArrayList<>();


        Map<String, operandRules> opcodeMap = new HashMap<String, operandRules>();
        opcodeMap.put("MOVE", new operandRules(moveOperandTypes));
        opcodeMap.put("MOVEI", new operandRules(moveImmediateOperandTypes));
        opcodeMap.put("ADD", new operandRules(mathOperandTypes));
        opcodeMap.put("INC", new operandRules(incDecOperandTypes));
        opcodeMap.put("SUB", new operandRules(mathOperandTypes));
        opcodeMap.put("DEC", new operandRules(incDecOperandTypes));
        opcodeMap.put("MUL", new operandRules(mathOperandTypes));
        opcodeMap.put("DIV", new operandRules(mathOperandTypes));
        opcodeMap.put("BEQ", new operandRules(conditionalBranchOperandTypes));
        opcodeMap.put("BLT", new operandRules(conditionalBranchOperandTypes));
        opcodeMap.put("BGT", new operandRules(conditionalBranchOperandTypes));
        opcodeMap.put("BR", new operandRules(unconditionalBranchOperandTypes));
        opcodeMap.put("END", new operandRules(endOperandTypes));

        return opcodeMap;
    }

    private static ArrayList<Integer> createOperandTypeArrays(String type) {
        ArrayList<Integer> createdArray = new ArrayList<>();
        if (type.equals("reg_mem")) {
            ArrayList<Integer> reg_mem = new ArrayList<Integer>() {{
                add(0);
                add(1);
            }};
            return reg_mem;
        }
        if (type.equals("reg_mem_immediate")) {
            ArrayList<Integer> reg_mem_immediate = new ArrayList<Integer>() {{
                add(0);
                add(1);
                add(2);
            }};
            return reg_mem_immediate;
        }

        if (type.equals("label")) {
            ArrayList<Integer> label = new ArrayList<Integer>() {{
                add(3);
            }};
            return label;
        }

        return createdArray;

    }

    private static boolean checkForLabel(String firstWord) {
        boolean hasLabel = false;
        if (firstWord.contains(":")) {
            hasLabel = true;
        }
        return hasLabel;
    }

    /**
     * Label should only be letters
     *
     * @param label - Every string before the first ":"
     * @return true if label is invalid
     */
    private static boolean validLabelOpcode(String label) {
        for (int i = 0; i < label.length(); i++) {
            Character letter = label.charAt(i);
            if (Character.isDigit(letter)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add the label to running labelList
     *
     * @param label - label to be added
     */
    private static void updateLabelList(String label) {
        labelList.add(label);
    }

    /**
     * Removes the lable from the current line - leaving only MAL code
     *
     * @param currentLineWordByWord - current line in array fo strings
     * @param lastLabelIndex        - where the last label is located in CurrentLineWordByWord array
     * @return - new line without labels
     */
    private static ArrayList<String> removeLabel(ArrayList<String> currentLineWordByWord, int lastLabelIndex) {
        ArrayList<String> newLine = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < currentLineWordByWord.size(); i++) {
            if (i > lastLabelIndex) {
                newLine.add(currentLineWordByWord.get(i).trim());
                j++;
            }
        }

        return newLine;
    }
}
