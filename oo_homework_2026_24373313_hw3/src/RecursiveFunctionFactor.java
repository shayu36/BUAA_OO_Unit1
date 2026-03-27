public class RecursiveFunctionFactor implements Factor {
    private final String order;
    private final Factor argument;
    
    public RecursiveFunctionFactor(String order, Factor argument) {
        this.order = order;
        this.argument = argument;
    }
    
    public String getOrder() {
        return order;
    }
    
    public Factor getArgument() {
        return argument;
    }
    
    @Override
    public Poly toPoly() {
        return
            RecursiveFunctionDef.expandCall(Integer.parseInt(order), argument.deepCopy()).toPoly();
    }
    
    @Override
    public Factor deepCopy() {
        return new RecursiveFunctionFactor(order, argument.deepCopy());
    }
    
    @Override
    public Factor substitute(Factor arg) {
        return new RecursiveFunctionFactor(order, argument.substitute(arg));
    }
    
}