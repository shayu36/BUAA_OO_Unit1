public interface Factor {
    Poly toPoly();
    
    Factor deepCopy();
    
    Factor substitute(Factor arg);
}