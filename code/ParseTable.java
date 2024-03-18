package code;

import java.util.ArrayList;

public class ParseTable {
    // store non-terminal
    public ArrayList<String> col;
    // store terminal
    public ArrayList<String> row;

    public String[][] table; 

    public ParseTable(){
        col = new ArrayList<>();
        row = new ArrayList<>();
        table = new String[100][100];
    }

    public void addCol(String newCol) {
        this.col.add(newCol);
    }

    public void addRow(String newRow) {
        this.row.add(newRow);
    }

    public void fillProduction(String row, String col, String production) {
        int colIndex = this.col.indexOf(col);
        int rowIndex = this.row.indexOf(row);
        table[rowIndex][colIndex] = production;
    }

    public void fillError(String row, String col) {
        int colIndex = this.col.indexOf(col);
        int rowIndex = this.row.indexOf(row);
        table[rowIndex][colIndex] = "error";
    }

    // return production in table by given col and row.
    public String getProduction(String row, String col) {
        int colIndex = this.col.indexOf(col);
        int rowIndex = this.row.indexOf(row);
        return table[rowIndex][colIndex];
    }

    public void printTable() {
        int i = 0, j = 0;
        for (;; i++) {
            for (;; j++) {
                if (table[i][j] == "") {
                    break;
                }
                System.out.println(table[i][j]);
            }
            j = 0;
            if (table[i][j] == "") {
                break;
            }
        }
    }

}
