package inputs.test023;

class A {
    public static void foo(int[][] a) {}
}

public class Test023 {
    public static void main(String[] args) {
        int[] a = new int[3];
        a[0] = 1;
        a[1] = 2;
        a[2] = 3;
        for (int i = 0; i < 3; i++) {
            System.out.println(a[i]);
        }
    }
}
