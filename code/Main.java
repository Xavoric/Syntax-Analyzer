package code;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        SyntaxParser parser = new SyntaxParser();
        parser.readCFG();
        parser.rebuildProduction();
        parser.fillParseTable();
        parser.analyze();
    }
}