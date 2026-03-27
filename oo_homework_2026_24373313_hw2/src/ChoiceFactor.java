public class ChoiceFactor implements Factor {
    private final Factor left;
    private final Factor right;
    private final Factor ansleft;
    private final Factor ansright;
    
    public ChoiceFactor(Factor left, Factor right, Factor ansleft, Factor ansright) {
        this.left = left;
        this.right = right;
        this.ansleft = ansleft;
        this.ansright = ansright;
    }
    
    @Override
    public Poly toPoly() {
        Poly leftPoly = left.toPoly().normalize();
        Poly rightPoly = right.toPoly().normalize();
        return leftPoly.equals(rightPoly)
            ? ansleft.toPoly().normalize()
            : ansright.toPoly().normalize();
    }
    
    @Override
    public Factor deepCopy() {
        return new ChoiceFactor(
            left.deepCopy(),
            right.deepCopy(),
            ansleft.deepCopy(),
            ansright.deepCopy()
        );
    }
    
    @Override
    public Factor substitute(Factor arg) {
        return new ChoiceFactor(
            left.substitute(arg),
            right.substitute(arg),
            ansleft.substitute(arg),
            ansright.substitute(arg)
        );
    }
    
    @Override
    public String toString() {
        return "[(" + left.toString() + "==" + right.toString() + ")?"
            + ansleft.toString() + ":" + ansright.toString() + "]";
    }
}