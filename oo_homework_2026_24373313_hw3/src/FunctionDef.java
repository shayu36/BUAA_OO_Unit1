public class FunctionDef {
    private static Expr body;
    
    public static Expr getBody() {
        return body;
    }
    
    public static void setBody(Expr expr) {
        String s = expr.toPoly().normalize().toString();
        Lexer lexer = new Lexer(s);
        Parser parser = new Parser(lexer);
        body = parser.parseExpr();
    }
}