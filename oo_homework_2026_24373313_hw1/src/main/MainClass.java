package main;

import expr.Expr;
import parser.Lexer;
import parser.Parser;
import poly.Poly;

import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String in = scanner.nextLine();
        Process process = new Process(in);
        String input = process.simplify(in);
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        Poly poly = expr.toPoly();
        String output = poly.mergeMono(poly.getMonoList());
        System.out.println(output);
    }
}
