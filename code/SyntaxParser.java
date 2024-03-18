package code;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class SyntaxParser {
    private ArrayList<String> terminal;
    private ArrayList<String> non_terminal;
    private ArrayList<String> initProduction;
    private ArrayList<String> finalProduction;
    private ArrayList<String> inputQueue;
    private Stack<String> parseStack;

    private ArrayList<String> symbol;

    private HashMap<String, ArrayList<String>> firstSet;
    private HashMap<String, ArrayList<String>> followSet;

    private ParseTable parseTable;

    private String CFGFile = "./code/CFG.txt";
    private String inputFile = "./code/input.txt";
    private String outputFile = "./code/output.txt";

    private BufferedReader CFGbr;
    private BufferedReader inputbr;
    private BufferedWriter bw;

    // read CFG, get terminal, non-terminal and original production
    public SyntaxParser() throws IOException {
        terminal = new ArrayList<>();
        non_terminal = new ArrayList<>();
        initProduction = new ArrayList<>();
        finalProduction = new ArrayList<>();
        inputQueue = new ArrayList<>();
        parseStack = new Stack<>();

        symbol = new ArrayList<>(List.of(
                "+", "-", "*", "/", "=", ">", "<", "!", "&", "|"));

        firstSet = new HashMap<>();
        followSet = new HashMap<>();

        parseTable = new ParseTable();
        File cfgfile = new File(CFGFile);
        FileReader cfgReader = new FileReader(cfgfile);
        CFGbr = new BufferedReader(cfgReader);

        File inputfile = new File(inputFile);
        FileReader inputReader = new FileReader(inputfile);
        inputbr = new BufferedReader(inputReader);

        File output = new File(outputFile);
        FileWriter writer = new FileWriter(output);
        bw = new BufferedWriter(writer);
    }

    public boolean isSymbol(String word) {
        return isTerminal(word) || isNonTerminal(word);
    }

    public boolean isTerminal(String word) {
        return terminal.contains(word);
    }

    public boolean isNonTerminal(String word) {
        return non_terminal.contains(word);
    }

    public String getNonTerminal(String production) {
        int i = 0;
        String nt = "";
        char c;
        for (; i < production.length() - 1; i++) {
            c = production.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }
            if (c == '-' && production.charAt(i + 1) == '>') {
                break;
            }
            nt += c;
        }
        return nt;
    }

    public String getFirst(String production) {
        String content = getProductionContent(production);
        String first = "";
        char c;
        for (int i = 0; i < content.length(); i++) {
            c = content.charAt(i);
            if (isSymbol(first) && !isSymbol(first + c)) {
                break;
            } else {
                first += c;
            }
        }
        return first;
    }

    public boolean matchFirstTerminal(String production, String terminal) {
        String first = getFirst(production);
        if (isTerminal(first)) {
            return first.equals(terminal);
        } else {
            boolean isMatched = false;
            for (String p : finalProduction) {
                if (!getNonTerminal(p).equals(first)) {
                    continue;
                }
                isMatched = matchFirstTerminal(p, terminal);
                if (isMatched) {
                    break;
                }
            }
            return isMatched;
        }
    }

    public String getProductionContent(String production) {
        int begin = 0;
        for (; begin < production.length(); begin++) {
            if (production.charAt(begin) == '-' && production.charAt(begin + 1) == '>') {
                begin += 2;
                break;
            }
        }
        int i = begin;
        String content = "";
        char c;
        for (; i < production.length(); i++) {
            c = production.charAt(i);
            content += c;
        }
        return content;
    }

    public int getCommonLeftFactorLength(String a, String b) {
        int begin = 0;
        for (; begin < a.length(); begin++) {
            if (a.charAt(begin) == '-' && a.charAt(begin + 1) == '>') {
                begin += 2;
                break;
            }
        }
        int i = begin;
        int minLength = Math.min(a.length() - 1, b.length() - 1);
        for (; i < minLength; i++) {
            char ca = a.charAt(i);
            char cb = b.charAt(i);
            if (ca != cb) {
                break;
            }
        }
        return i - begin;
    }

    public int getCommonLeftFactorIndex(String a, String b) {
        int begin = 0;
        for (; begin < a.length(); begin++) {
            if (a.charAt(begin) == '-' && a.charAt(begin + 1) == '>') {
                begin += 2;
                break;
            }
        }
        int i = begin;
        int minLength = Math.min(a.length() - 1, b.length() - 1);
        for (; i < minLength; i++) {
            char ca = a.charAt(i);
            char cb = b.charAt(i);
            if (ca != cb) {
                break;
            }
        }
        return i;
    }

    public String getCommonLeftFactorString(String a, String b) {
        int begin = 0;
        for (; begin < a.length(); begin++) {
            if (a.charAt(begin) == '-' && a.charAt(begin + 1) == '>') {
                begin += 2;
                break;
            }
        }
        int i = begin;
        int minLength = Math.min(a.length(), b.length());
        for (; i < minLength; i++) {
            char ca = a.charAt(i);
            char cb = b.charAt(i);
            if (ca != cb) {
                break;
            }
        }
        return a.substring(begin, i);
    }

    public void readCFG() throws IOException {
        String nt = "";
        String t = "";
        char c;
        // read terminal
        while ((c = (char) CFGbr.read()) != '\r') {
            if (c == ',') {
                terminal.add(t);
                // parseTable.addCol(t);
                t = "";
                continue;
            }
            if (c == ' ') {
                continue;
            }
            t += c;
        }
        terminal.add(t);
        terminal.add("#");
        CFGbr.read();

        // read non-terminal
        while ((c = (char) CFGbr.read()) != '\r') {
            if (c == ',') {
                non_terminal.add(nt);
                nt = "";
                continue;
            }
            if (c == ' ') {
                continue;
            }
            nt += c;
        }
        non_terminal.add(nt);
        CFGbr.read();

        // read productions
        int charCode;
        String production = "";
        while ((charCode = CFGbr.read()) != -1) {
            c = (char) charCode;
            if (c == '\n') {
                initProduction.add(production);
                production = "";
            } else {
                production += c;
            }
        }
        initProduction.add(production + "\r");

        CFGbr.close();
    }

    // eilminate left recursion and extract maximum common left factors
    public void rebuildProduction() {
        ArrayList<String> NeedReduceLeftR = new ArrayList<>();
        String nt = "";
        String word = "";
        char c;
        boolean isReadingContent = false;
        // 1.Elimate left recursion
        for (String singleProduction : initProduction) {
            isReadingContent = false;
            word = "";
            nt = "";
            for (int i = 0; i < singleProduction.length() - 1; i++) {
                c = singleProduction.charAt(i);
                if (Character.isWhitespace(c)) {
                    continue;
                }

                if (c == '-' && singleProduction.charAt(i + 1) == '>') {
                    isReadingContent = true;
                    i += 1;
                    continue;
                }

                if (!isReadingContent) {
                    nt += c;
                } else {
                    word += c;
                    // find first symbol
                    if (isSymbol(word) && !isSymbol(word + singleProduction.charAt(i + 1))) {
                        if (word.equals(nt)) {
                            if (!NeedReduceLeftR.contains(nt)) {
                                NeedReduceLeftR.add(nt);
                                non_terminal.add(word + "'");
                                // use # as ε
                                finalProduction.add(nt + "'->#");
                            }
                            word = "";
                            for (int j = i + 1; j < singleProduction.length() - 1; j++) {
                                if (singleProduction.charAt(j) == '\r') {
                                    break;
                                }
                                word += singleProduction.charAt(j);
                            }
                            word += nt + "'";
                            finalProduction.add(nt + "'->" + word.replaceAll("\\s", ""));
                        } else {
                            if (NeedReduceLeftR.contains(nt)) {
                                finalProduction.add(singleProduction.replaceAll("\\s", "") + nt + "'");
                            } else {
                                finalProduction.add(singleProduction.replaceAll("\\s", ""));
                            }
                            break;
                        }
                    }
                }
            }
        }

        // 2.extract maximum common left factors
        for (int i = 0; i < finalProduction.size(); i++) {
            for (int j = i + 1; j < finalProduction.size(); j++) {
                String Pa = finalProduction.get(i);
                String Pb = finalProduction.get(j);
                String nta = getNonTerminal(Pa);
                String ntb = getNonTerminal(Pb);
                if (!nta.equals(ntb)) {
                    continue;
                }
                int commonLength = getCommonLeftFactorLength(Pa, Pb);
                if (commonLength != 0) {
                    String common = getCommonLeftFactorString(Pa, Pb);
                    String restA = "";
                    String restB = "";
                    int restBegin = getCommonLeftFactorIndex(Pa, Pb) + 1;
                    for (int ra = restBegin; ra < Pa.length(); ra++) {
                        restA += Pa.charAt(ra);
                    }
                    for (int rb = restBegin; rb < Pb.length(); rb++) {
                        restB += Pb.charAt(rb);
                    }
                    String newP = nta + "'->";
                    if (restA.isEmpty()) {
                        finalProduction.add(newP + '#');
                    } else {
                        finalProduction.add(newP + restA);
                    }
                    if (restB.isEmpty()) {
                        finalProduction.add(newP + '#');
                    } else {
                        finalProduction.add(newP + restB);
                    }

                    finalProduction.remove(Pa);
                    finalProduction.remove(Pb);
                    finalProduction.add(nta + "->" + common + nta + "'");
                    non_terminal.add(nta + "'");
                }
            }
        }
        // sort the productions by first character
        Collections.sort(finalProduction);

        for (String nont : non_terminal) {
            firstSet.put(nont, new ArrayList<>());
            followSet.put(nont, new ArrayList<>());
        }
    }

    public void calculateFirstSet(String nonTerminal) {
        String first = "";
        for (String singleP : finalProduction) {
            if (!getNonTerminal(singleP).equals(nonTerminal)) {
                continue;
            }
            first = getFirst(singleP);
            if (isNonTerminal(first)) {
                calculateFirstSet(first);
                for (String str : firstSet.get(first)) {
                    if (!firstSet.get(nonTerminal).contains(str)) {
                        firstSet.get(nonTerminal).add(str);
                    }
                }
            } else {
                if (!firstSet.get(nonTerminal).contains(first)) {
                    firstSet.get(nonTerminal).add(first);
                }
            }
        }

    }

    public void calculateFollowSet() {
        for (String production : finalProduction) {
            String nt = "";
            String follow = "";
            char c;
            int i = 0;
            for (; i < production.length(); i++) {
                c = production.charAt(i);
                if (c == '-' && production.charAt(i + 1) == '>') {
                    break;
                }
            }
            i += 2;
            for (; i < production.length(); i++) {
                c = production.charAt(i);
                if (isSymbol(follow) && !isSymbol(follow + c)) {
                    if (isNonTerminal(follow)) {
                        if (nt == "") {
                            nt = follow;

                        } else {
                            for (String first : firstSet.get(follow)) {
                                if (!followSet.get(nt).contains(first)) {
                                    followSet.get(nt).add(first);
                                }
                            }
                            nt = follow;
                        }
                        follow = "";
                        follow += c;
                        continue;
                    }
                    if (isTerminal(follow)) {
                        if (nt == "") {
                            follow = "";
                            follow += c;
                        } else {
                            if (!followSet.get(nt).contains(follow)) {
                                followSet.get(nt).add(follow);
                            }
                            follow = "";
                            follow += c;
                            nt = "";
                        }
                        continue;
                    }
                } else {
                    follow += c;
                }
            }
            if (nt != "") {
                if (isNonTerminal(follow)) {
                    for (String first : firstSet.get(follow)) {
                        if (!followSet.get(nt).contains(first)) {
                            followSet.get(nt).add(first);
                        }
                    }
                } else {
                    if (!followSet.get(nt).contains(follow)) {
                        followSet.get(nt).add(follow);
                    }
                }
            }

            if (isNonTerminal(follow)) {
                if (!followSet.get(follow).contains("#")) {
                    followSet.get(follow).add("#");
                }
            }
        }

        for (String production : finalProduction) {
            String begin="";
            String nt = "";
            String follow = "";
            char c;
            int i = 0;
            for (; i < production.length(); i++) {
                c = production.charAt(i);
                if (c == '-' && production.charAt(i + 1) == '>') {
                    break;
                }
                begin+=c;
            }
            i += 2;
            for (; i < production.length(); i++) {
                c = production.charAt(i);
                if (isSymbol(follow) && !isSymbol(follow + c)) {
                    if (isNonTerminal(follow)) {
                        nt=follow;
                        follow = "";
                        follow += c;
                        continue;
                    }
                    if (isTerminal(follow)) {
                        follow = "";
                        follow += c;
                        if (nt != "") {
                            nt = "";
                        }
                        continue;
                    }
                } else {
                    follow += c;
                }
            }

            if (isNonTerminal(follow)) {
                for(String flw : followSet.get(begin)){
                    if (!followSet.get(follow).contains(flw)) {
                        followSet.get(follow).add(flw);
                    }
                }
            }
        }
    }

    public void fillParseTable() {
        // 1.Compute First Set and Follow Set using finalProduction.
        for (String nt : non_terminal) {
            calculateFirstSet(nt);
        }
        // 2.Compute Follow Set by finalProduction and First Set
        calculateFollowSet();

        for (String c : terminal) {
            parseTable.addCol(c);
        }

        for (String r : non_terminal) {
            parseTable.addRow(r);
        }

        // 3.Fill the LL(1) ParseTable
        parseTable.fillProduction("S", "#", "Accept");

        for (String key : firstSet.keySet()) {
            for (String first : firstSet.get(key)) {
                if (!first.equals("#")) {
                    String production = "";
                    for (String p : finalProduction) {
                        if (!getNonTerminal(p).equals(key)) {
                            continue;
                        }
                        if (matchFirstTerminal(p, first)) {
                            production = p;
                            break;
                        }
                    }
                    parseTable.fillProduction(key, first, production);

                } else {
                    for (String follow : followSet.get(key)) {
                        String production = "";
                        for (String p : finalProduction) {
                            if (!getNonTerminal(p).equals(key)) {
                                continue;
                            }
                            if (matchFirstTerminal(p, first)) {
                                production = p;
                                break;
                            }
                        }
                        parseTable.fillProduction(key, follow, production);

                    }
                }
            }
        }
    }

    public boolean isLetter(char token) {
        if ((token >= 'a' && token <= 'z') || (token >= 'A' && token <= 'Z') || (token == '_')) {
            return true;
        } else
            return false;
    }

    public boolean isDigit(char token) {
        if (token >= '0' && token <= '9') {
            return true;
        } else
            return false;
    }

    public boolean isTerminalSymbol(char token) {
        return symbol.contains(token + "");
    }

    public boolean isBracket(char token) {
        if (token == '(' || token == ')' || token == '{' || token == '}') {
            return true;
        } else
            return false;
    }

    // prepare before analyzing
    public void readInput() throws IOException {
        String word = "";
        boolean isReadingWord = false;
        boolean isReadingValue = false;
        boolean isReadingSymbol = false;
        int c;

        while ((c = inputbr.read()) != -1) {
            char token = (char) c;
            if (isBracket(token)) {
                if (word != "") {
                    if (isReadingWord) {
                        if (terminal.contains(word)) {
                            inputQueue.add(word);
                        } else {
                            inputQueue.add("identifier");
                        }
                    }
                    if (isReadingValue) {
                        inputQueue.add("value");
                    }
                    if (isReadingSymbol) {
                        inputQueue.add(word);
                    }
                    word = "";
                    isReadingSymbol = false;
                    isReadingValue = false;
                    isReadingWord = false;
                }
                inputQueue.add(token + "");
                continue;
            }
            if (word.equals("=") && token != '=') {
                inputQueue.add(word);
                word = "";
                word += token;
                isReadingSymbol = false;
                if (isTerminalSymbol(token)) {
                    isReadingSymbol = true;
                }
                if (isDigit(token)) {
                    isReadingValue = true;
                }
                if (isLetter(token)) {
                    isReadingWord = true;
                }
                continue;
            }
            if (Character.isWhitespace(token) || token == ';') {
                if (word != "") {
                    if (isReadingWord) {
                        if (terminal.contains(word)) {
                            inputQueue.add(word);
                        } else {
                            inputQueue.add("identifier");
                        }
                    }
                    if (isReadingValue) {
                        inputQueue.add("value");
                    }
                    if (isReadingSymbol) {
                        inputQueue.add(word);
                    }
                    word = "";
                    isReadingSymbol = false;
                    isReadingValue = false;
                    isReadingWord = false;
                }
                continue;
            }

            if (!isReadingWord && !isReadingValue && !isReadingSymbol) {
                if (isLetter(token)) {
                    isReadingWord = true;
                    word += token;
                }
                if (isDigit(token)) {
                    isReadingValue = true;
                    word += token;
                }
                if (isTerminalSymbol(token)) {
                    isReadingSymbol = true;
                    word += token;
                }
                continue;
            }

            if (isReadingWord) {
                if (isTerminalSymbol(token)) {
                    if (terminal.contains(word)) {
                        inputQueue.add(word);
                    } else {
                        inputQueue.add("identifier");
                    }
                    word = "";
                    word += token;
                    isReadingWord = false;
                    isReadingSymbol = true;
                } else {
                    word += token;
                }
                continue;
            }

            if (isReadingValue) {
                if (isTerminalSymbol(token)) {
                    inputQueue.add("value");
                    isReadingValue = false;
                    isReadingSymbol = true;
                    word = "";
                    word += token;
                }
                if (isLetter(token)) {
                    System.out.println("Illegal value:" + word + token);
                    break;
                }
                if (isDigit(token)) {
                    word += token;
                }
                continue;
            }

            if (isReadingSymbol) {
                if (isTerminalSymbol(token)) {
                    word += token;
                } else {
                    inputQueue.add(word);
                    word = "";
                    word += token;
                    if (isLetter(token)) {
                        isReadingWord = true;
                    }
                    if (isDigit(token)) {
                        isReadingValue = true;
                    }
                    isReadingSymbol = false;
                }
                continue;
            }

        }
        inputQueue.add("#");

        for (String w : inputQueue) {
            System.out.println("Read: " + w);
        }
    }

    // push a production into stack
    public void reduceProduction(String production) {
        String content = getProductionContent(production);
        Stack<String> tempStack = new Stack<>();
        int i = 0;
        String word = "";
        char c;
        for (; i < content.length(); i++) {
            c = content.charAt(i);
            if (isSymbol(word) && !isSymbol(word + c)) {
                tempStack.push(word);
                word = "";
                word += c;
            } else {
                word += c;
            }
        }
        if (word != "") {
            tempStack.push(word);
        }

        while (!tempStack.isEmpty()) {
            String a = tempStack.pop();
            parseStack.push(a);
        }
    }

    // read input and use LL(1) parse table to analyze
    public void analyze() throws IOException {
        readInput();
        parseStack.push("#");
        parseStack.push("S");
        boolean isAccept = false;
        String production = "";
        while (!isAccept) {
            // get top element of the stack
            String top = parseStack.elementAt(parseStack.size() - 1);
            if (top.equals("#")) {
                if (inputQueue.get(0).equals("#")) {
                    isAccept = true;
                    System.out.println("ACCEPT");
                    bw.write("ACCEPT"+"\n");
                    break;
                } else {
                    parseStack.pop();
                    continue;
                }
            }

            if (top.equals(inputQueue.get(0))) {
                System.out.println("MATCH: " + inputQueue.get(0));
                bw.write("MATCH: " + inputQueue.get(0)+"\n");

                parseStack.pop();
                inputQueue.remove(0);
                continue;
            } else {
                parseStack.pop();
                production = parseTable.getProduction(top, inputQueue.get(0));
                if (production == null) {
                    production = parseTable.getProduction(top, "#");
                }
                System.out.println("OUTPUT: " + production);
                bw.write("OUTPUT: " + production+"\n");

                reduceProduction(production);
            }
        }
        bw.close();
    }

}
