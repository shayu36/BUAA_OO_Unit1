public class DerivativeFactor implements Factor {
    private final String type; // "dx" / "dy" / "grad"
    private final Expr expr;
    
    public DerivativeFactor(String type, Expr expr) {
        this.type = type;
        this.expr = expr;
    }
    
    public String getType() {
        return type;
    }
    
    public Expr getExpr() {
        return expr;
    }
    
    @Override
    public Factor deepCopy() {
        return new DerivativeFactor(type, (Expr) expr.deepCopy());
    }
    
    @Override
    public Factor substitute(Factor arg) {
        return new DerivativeFactor(type, (Expr) expr.substitute(arg));
    }
    
    @Override
    public Poly toPoly() {
        Poly inner = expr.toPoly();
        switch (type) {
            case "dx":
                return inner.derivative('x');
            case "dy":
                return inner.derivative('y');
            case "grad":
                return inner.derivative('x').addPoly(inner.derivative('y'));
            default:
                throw new IllegalArgumentException("unknown derivative type: " + type);
        }
    }
}