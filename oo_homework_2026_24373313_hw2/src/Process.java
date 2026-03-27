public class Process {
    
    public Process(String input) {
    }
    
    public String simplify(String input) {
        String s = input;
        s = s.replaceAll("\\s+", "");
        s = s.replaceAll("\\^\\+", "^");
        
        String prev;
        do {
            prev = s;
            s = s.replace("++", "+");
            s = s.replace("+-", "-");
            s = s.replace("-+", "-");
            s = s.replace("--", "+");
        } while (!s.equals(prev));
        
        return s;
    }
}
