import java.util.ArrayList;

public class Expr {
    private final ArrayList<Term> terms;
    private final ArrayList<Token> signs;
    
    public Expr() {
        this.terms = new ArrayList<>();
        this.signs = new ArrayList<>();
    }
    
    public void addTerm(Term term) {
        this.terms.add(term);
    }
    
    public void addSign(Token sign) {
        this.signs.add(sign);
    }
    
    public static Expr fromFactor(Factor factor) {
        Expr expr = new Expr();
        Term term = new Term();
        term.setSign(new Token(Token.Type.ADD, "+"));
        term.addFactor(factor);
        expr.addSign(new Token(Token.Type.ADD, "+"));
        expr.addTerm(term);
        return expr;
    }
    
    public Expr deepCopy() {
        Expr res = new Expr();
        for (Token sign : signs) {
            res.addSign(new Token(Token.Type.ADD, sign.getContent()));
        }
        for (Term term : terms) {
            res.addTerm(term.deepCopy());
        }
        return res;
    }
    
    public Expr substitute(Factor arg) {
        Expr res = new Expr();
        for (Token sign : signs) {
            res.addSign(new Token(Token.Type.ADD, sign.getContent()));
        }
        for (Term term : terms) {
            res.addTerm(term.substitute(arg));
        }
        return res;
    }
    
    public Poly toPoly() {
        Poly res = new Poly(new ArrayList<>());
        
        for (int i = 0; i < terms.size(); i++) {
            Poly cur = terms.get(i).toPoly();
            if (signs.get(i).getContent().equals("+")) {
                res = res.addPoly(cur);
            } else {
                res = res.subPoly(cur);
            }
        }
        return res;
    }
    
    @Override
    public String toString() {
        if (terms.isEmpty()) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < terms.size(); i++) {
            String sign = signs.get(i).getContent();
            String termStr = terms.get(i).toString();
            
            if (i == 0) {
                if (sign.equals("-")) {
                    sb.append("-");
                }
                sb.append(termStr);
            } else {
                sb.append(sign).append(termStr);
            }
        }
        return sb.toString();
    }
}