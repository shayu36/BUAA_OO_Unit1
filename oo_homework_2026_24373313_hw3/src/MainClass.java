import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = Integer.parseInt(scanner.nextLine().trim());
        if (n == 1) {
            String funcLine = scanner.nextLine().trim();
            readFunctionDefinition(funcLine);
        }
        int m = Integer.parseInt(scanner.nextLine().trim());
        if (m == 1) {
            readRecursiveFunctionDefinition(scanner);
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
    
    private static void readRecursiveFunctionDefinition(Scanner scanner) {
        for (int i = 0; i < 3; i++) {
            String line = scanner.nextLine().trim();
            line = line.replaceAll("\\s+", "");
            int eqPos = line.indexOf('=');
            String left = line.substring(0, eqPos).trim();
            String right = line.substring(eqPos + 1).trim();
            Process process = new Process(right);
            String input = process.simplify(right);
            if (left.contains("0")) {
                Lexer lexer = new Lexer(input);
                Parser parser = new Parser(lexer);
                Expr expr = parser.parseExpr();
                RecursiveFunctionDef.setF0(expr);
            } else if (left.contains("1")) {
                Lexer lexer = new Lexer(input);
                Parser parser = new Parser(lexer);
                Expr expr = parser.parseExpr();
                RecursiveFunctionDef.setF1(expr);
            } else {
                RecursiveFunctionDef.setFn(input);
            }
        }
    }
}
/*debBUBUBUBUBUG
0
1
f{0}(x)=-12*exp((x*(-exp(x)*012))^7)
f{1}(x)=-(x^0*3+18*5)^6*(-12*-8*-4+x)
f{n}(x)=7*f{n-1}(x)-2*f{n-2}(x)+1
-f{2}((x+1)^2)
* */