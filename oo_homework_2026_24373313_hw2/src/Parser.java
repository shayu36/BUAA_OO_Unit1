import java.math.BigInteger;

public class Parser {
    private final Lexer lexer;
    private final Token add = new Token(Token.Type.ADD, "+");
    private final Token sub = new Token(Token.Type.SUB, "-");
    
    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }
    
    public Expr parseExpr() {
        Expr expr = new Expr();
        expr.addSign(add);
        expr.addTerm(parseTerm());
        
        while (!lexer.isEnd() && (lexer.peek().equals("+") || lexer.peek().equals("-"))) {
            if (lexer.peek().equals("-")) {
                expr.addSign(sub);
            } else {
                expr.addSign(add);
            }
            lexer.next();
            expr.addTerm(parseTerm());
        }
        return expr;
    }
    
    public Term parseTerm() {
        Term term = new Term();
        term.setSign(add);
        
        // 第一个 factor 前允许有一元 +/-
        if (lexer.peek().equals("+")) {
            lexer.next();
        } else if (lexer.peek().equals("-")) {
            term.addFactor(new Number(BigInteger.valueOf(-1)));
            lexer.next();
        }
        term.addFactor(parseFactor());
        
        while (!lexer.isEnd() && lexer.peek().equals("*")) {
            lexer.next();
            
            // * 后面的 factor 前也允许有一元 +/-
            if (lexer.peek().equals("+")) {
                lexer.next();
            } else if (lexer.peek().equals("-")) {
                term.addFactor(new Number(BigInteger.valueOf(-1)));
                lexer.next();
            }
            
            term.addFactor(parseFactor());
        }
        return term;
    }
    
    public Factor parseFactor() {
        if (lexer.peek().equals("(")) {
            lexer.next();
            Expr expr = parseExpr();
            lexer.next();
            return new ExprFactor(expr, parseIndex());
        } else if (lexer.peek().equals("+") || lexer.peek().equals("-")
            || Character.isDigit(lexer.peek().charAt(0))) {
            return parseNumberFactor();
        } else if (lexer.peek().equals("exp")) {
            lexer.next();
            lexer.next();
            Factor factor = parseFactor();
            lexer.next();
            return new ExpFactor(factor, parseIndex());
        } else if (lexer.peek().equals("[")) {
            lexer.next();
            lexer.next();
            Factor left;
            left = parseFactor();
            lexer.next();
            Factor right;
            right = parseFactor();
            lexer.next();
            lexer.next();
            Factor ansleft = parseFactor();
            lexer.next();
            Factor ansright = parseFactor();
            lexer.next();
            return new ChoiceFactor(left, right, ansleft, ansright);
        } else if (lexer.peek().equals("f")) {
            lexer.next();
            lexer.next();
            Factor argument = parseFactor();
            lexer.next();
            return new FunctionFactor(argument);
        } else {
            String name = lexer.peek();
            lexer.next();
            return new Var(name, parseIndex());
        }
    }
    
    private BigInteger parseIndex() {
        if (!lexer.isEnd() && lexer.peek().equals("^")) {
            lexer.next();
            BigInteger exp = new BigInteger(lexer.peek());
            lexer.next();
            return exp;
        } else {
            return BigInteger.ONE;
        }
    }
    
    public Number parseNumberFactor() {
        int sign = 1;
        if (lexer.peek().equals("+")) {
            lexer.next();
        } else if (lexer.peek().equals("-")) {
            sign = -1;
            lexer.next();
        }
        BigInteger num = new BigInteger(lexer.peek());
        lexer.next();
        if (sign == -1) {
            num = num.negate();
        }
        return new Number(num);
    }
}