package inputs.test021;

import inputs.test019.Test019;

class A {
    A() {}
}

class B extends A {

    B() {
        super();
    }
}

public class Test021 {

    private int x = 3, y = 4;
    private int[] z;
    private String s = "testing this";
    private char c = 'a';

    Test021(int i) {}

    static int x() {
        return 4;
    }
}
