import java.math.BigInteger;

public class RecursiveFunctionDef {
    private static final Expr[] templates = new Expr[6];
    private static Expr f0;
    private static Expr f1;
    private static BigInteger coef1;
    private static Factor arg1;
    private static BigInteger coef2;
    private static Factor arg2;
    private static Expr extra;
    
    // input: coef1*f{n-1}(arg1)+coef2*f{n-2}(arg2)+extra
    public static void setFn(String input) {
        String mark1 = "*f{n-1}(";
        int p1 = input.indexOf(mark1);
        coef1 = new BigInteger(input.substring(0, p1));
        
        int arg1Left = p1 + mark1.length() - 1;
        int arg1Right = findMatchingParenthesis(input, arg1Left);
        String arg1Str = input.substring(arg1Left + 1, arg1Right);
        arg1 = simplifyFactor(parseFactor(arg1Str));
        
        char sign2 = input.charAt(arg1Right + 1);
        
        String mark2 = "*f{n-2}(";
        int p2 = input.indexOf(mark2, arg1Right + 2);
        coef2 = new BigInteger(input.substring(arg1Right + 2, p2));
        if (sign2 == '-') {
            coef2 = coef2.negate();
        }
        
        int arg2Left = p2 + mark2.length() - 1;
        int arg2Right = findMatchingParenthesis(input, arg2Left);
        String arg2Str = input.substring(arg2Left + 1, arg2Right);
        arg2 = simplifyFactor(parseFactor(arg2Str));
        
        if (arg2Right == input.length() - 1) {
            extra = null;
        } else {
            String extraStr = input.substring(arg2Right + 1);
            extra = parseExpr("0" + extraStr);
        }
    }
    
    private static Expr parseExpr(String s) {
        Lexer lexer = new Lexer(s);
        Parser parser = new Parser(lexer);
        return parser.parseExpr();
    }
    
    private static Factor parseFactor(String s) {
        Lexer lexer = new Lexer(s);
        Parser parser = new Parser(lexer);
        return parser.parseFactor();
    }
    
    private static int findMatchingParenthesis(String s, int left) {
        int cnt = 0;
        for (int i = left; i < s.length(); i++) {
            if (s.charAt(i) == '(') {
                cnt++;
            } else if (s.charAt(i) == ')') {
                cnt--;
                if (cnt == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    private static Expr getTemplate(int order) {
        if (templates[order] != null) {
            return templates[order].deepCopy();
        }
        if (order == 0) {
            templates[0] = f0.deepCopy();
            return templates[0].deepCopy();
        }
        if (order == 1) {
            templates[1] = f1.deepCopy();
            return templates[1].deepCopy();
        }
        
        Expr leftExpr = getTemplate(order - 1).substitute(arg1.deepCopy());
        Expr rightExpr = getTemplate(order - 2).substitute(arg2.deepCopy());
        Poly leftPoly = leftExpr.toPoly().mulConst(coef1);
        Poly rightPoly = rightExpr.toPoly().mulConst(coef2);
        Poly extraPoly = (extra == null) ? Poly.zero() : extra.toPoly().normalize();
        Poly sum = leftPoly.addPoly(rightPoly).addPoly(extraPoly).normalize();
        templates[order] = parseExpr(sum.toString());
        return templates[order].deepCopy();
    }
    
    public static Expr expandCall(int order, Factor actualArg) {
        Factor simplifiedArg = simplifyFactor(actualArg.deepCopy());
        return getTemplate(order).substitute(simplifiedArg);
    }
    
    public static void setF0(Expr expr) {
        f0 = simplifyExpr(expr);
    }
    
    public static void setF1(Expr expr) {
        f1 = simplifyExpr(expr);
    }
    
    private static Expr simplifyExpr(Expr expr) {
        return parseExpr(expr.toPoly().normalize().toString());
    }
    
    private static Factor simplifyFactor(Factor factor) {
        Expr expr = parseExpr(factor.toPoly().normalize().toString());
        return new ExprFactor(expr, BigInteger.ONE);
    }
}