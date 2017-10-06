namespace java
{
namespace lang
{

// ======================================================================

// For simplicity, we use C++ inheritance for exceptions and throw
// them by value.  In other words, the translator does not support
// user-defined exceptions and simply relies on a few built-in
// classes.
class Throwable
{
};

class Exception : public Throwable
{
};

class RuntimeException : public Exception
{
};

class NullPointerException : public RuntimeException
{
};

class NegativeArraySizeException : public RuntimeException
{
};

class ArrayStoreException : public RuntimeException
{
};

class ClassCastException : public RuntimeException
{
};

class IndexOutOfBoundsException : public RuntimeException
{
};

class ArrayIndexOutOfBoundsException : public IndexOutOfBoundsException
{
};

}
}
