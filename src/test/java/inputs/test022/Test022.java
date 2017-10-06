package inputs.test022;

class A {
    public int i;
    A() {
        i = 2;
    }
    void foo(int i) {}
}

public class Test022 {

    public static void main(String[] args) {
        A a = new A();
        System.out.println(a.i);
        System.out.println(a.getClass().toString());
    }
}
