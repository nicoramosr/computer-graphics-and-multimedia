public class CoordinatesConverter {
    public static void main(String[] args) {
        //polar to cartesian
        double r = 5.0;
        double theta = Math.PI / 4; // 45 degrees in radians
        double x = r * Math.cos(theta);
        double y = r * Math.sin(theta);
        System.out.println("Cartesian coordinates: (" + x + ", " + y + ")");

        //cartesian to polar
        x = 3.0;
        y = 4.0;
        r = Math.sqrt(x * x + y * y);
        theta = Math.atan2(y, x);
        System.out.println("Polar coordinates: (r=" + r + ", theta=" + theta + ")");
    }
}
