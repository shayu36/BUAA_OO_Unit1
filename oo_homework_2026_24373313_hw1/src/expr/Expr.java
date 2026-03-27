package expr;

import parser.Token;
import poly.Mono;
import poly.Poly;

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

    public Poly toPoly() {
        Poly res = new Poly(new ArrayList<Mono>());

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
}
