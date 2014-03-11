import kotlin.jvm.KotlinSignature;

interface A {
    void foo(int x);
}

interface B {
    void bar(String a, double b);
}


interface C {
    @KotlinSignature("fun foo(signatureName: String): Unit")
    void foo(String codeName);
}
