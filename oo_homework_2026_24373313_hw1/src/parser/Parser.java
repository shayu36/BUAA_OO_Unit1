package parser;

import expr.Expr;
import expr.ExprFactor;
import expr.Factor;
import expr.Number;
import expr.Term;
import expr.Var;

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

        if (lexer.peek().equals("+")) {
            lexer.next();
        } else if (lexer.peek().equals("-")) {
            term.addFactor(new Number(BigInteger.valueOf(-1)));
            lexer.next();
        }
        term.addFactor(parseFactor());

        while (!lexer.isEnd() && lexer.peek().equals("*")) {
            lexer.next();

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

            if (lexer.isEnd() || !lexer.peek().equals(")")) {
                throw new RuntimeException("missing )");
            }
            lexer.next();

            if (!lexer.isEnd() && lexer.peek().equals("^")) {
                lexer.next();
                BigInteger exp = new BigInteger(lexer.peek());
                lexer.next();
                return new ExprFactor(expr, exp);
            } else {
                return new ExprFactor(expr, BigInteger.ONE);
            }
        } else if (Character.isDigit(lexer.peek().charAt(0))) {
            BigInteger num = new BigInteger(lexer.peek());
            lexer.next();
            return new Number(num);
        } else {
            String name = lexer.peek();
            lexer.next();
            if (!lexer.isEnd() && lexer.peek().equals("^")) {
                lexer.next();
                BigInteger exp = new BigInteger(lexer.peek());
                lexer.next();
                return new Var(name, exp);
            } else {
                return new Var(name, BigInteger.ONE);
            }
        }
    }
}
