public class FunctionFactor implements Factor {
    private final Factor argument;
    
    public FunctionFactor(Factor argument) {
        this.argument = argument;
    }
    
    @Override
    public Poly toPoly() {
        Expr bodyCopy = FunctionDef.getBody().deepCopy();
        Expr substituted = bodyCopy.substitute(argument.deepCopy());
        return substituted.toPoly().normalize();
    }
    
    @Override
    public Factor deepCopy() {
        return new FunctionFactor(argument.deepCopy());
    }
    
    @Override
    public Factor substitute(Factor arg) {
        return new FunctionFactor(argument.substitute(arg));
    }
    
    @Override
    public String toString() {
        return "f(" + argument.toString() + ")";
    }
}