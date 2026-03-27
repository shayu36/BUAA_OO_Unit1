public class FunctionDef {
    private static Expr body;
    
    public static void setBody(Expr expr) {
        body = expr;
    }
    
    public static Expr getBody() {
        return body;
    }
    
    public static boolean isDefined() {
        return body != null;
    }
}