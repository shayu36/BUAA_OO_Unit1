import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = Integer.parseInt(scanner.nextLine().trim());
        if (n == 1) {
            String funcLine = scanner.nextLine().trim();
            readFunctionDefinition(funcLine);
        }
        String in = scanner.nextLine().trim();
        Process process = new Process(in);
        String input = process.simplify(in);
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        Poly poly = expr.toPoly().normalize();
        String output = poly.toString();
        System.out.println(output);
    }
    
    private static void readFunctionDefinition(String line) {
        int eqPos = line.indexOf('=');
        String right = line.substring(eqPos + 1).trim();
        Process process = new Process(right);
        String input = process.simplify(right);
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        FunctionDef.setBody(expr);
    }
}