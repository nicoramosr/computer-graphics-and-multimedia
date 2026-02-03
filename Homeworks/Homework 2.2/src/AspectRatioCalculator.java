public class AspectRatioCalculator {
    public static void main(String[] args) {
        int width = 1720;
        int height = 1080;
        int gcd = gcd(width, height);
        int aspectWidth = width / gcd;
        int aspectHeight = height / gcd;
        System.out.println("Aspect Ratio: " + aspectWidth + ":" + aspectHeight);
    }
    
    private static int gcd(int a, int b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }
}