#pragma once

#include <stdint.h>
#include <string>
#include "java_lang.h"

using namespace java::lang;

namespace inputs {

    namespace test006 {

        struct __A;
        struct __A_VT;

        typedef __rt::Ptr<__A> A;

        struct __A {
            __A_VT* __vptr;
            static __A_VT vtable;
            static Class __class();
            String fld;
            __A();
            static void init__A(__A*);
            static void setFld(A, String);
            static void almostSetFld(A, String);
            static String getFld(A);
        };

        struct __A_VT {
            Class __isa;
            void (*__delete)(__A*);
            int32_t (*hashCode)(A);
            bool (*equals)(A, Object);
            Class (*getClass)(A);
            String (*toString)(A);
            void (*init__A)(__A*);
            void (*setFld)(A, String);
            void (*almostSetFld)(A, String);
            String (*getFld)(A);
            __A_VT() : 
                __isa(__A::__class()),
                __delete(&__rt::__delete<__A>),
                hashCode((int32_t(*)(A))&__Object::hashCode),
                equals((bool(*)(A, Object))&__Object::equals),
                getClass((Class(*)(A))&__Object::getClass),
                toString((String(*)(A))&__Object::toString),
                init__A(&__A::init__A),
                setFld(&__A::setFld),
                almostSetFld(&__A::almostSetFld),
                getFld(&__A::getFld) {
                }
        };

        struct __Test006;
        struct __Test006_VT;

        typedef __rt::Ptr<__Test006> Test006;

        struct __Test006 {
            __Test006_VT* __vptr;
            static __Test006_VT vtable;
            static Class __class();
            __Test006();
            static void init__Test006(__Test006*);
        };

        struct __Test006_VT {
            Class __isa;
            void (*__delete)(__Test006*);
            int32_t (*hashCode)(Test006);
            bool (*equals)(Test006, Object);
            Class (*getClass)(Test006);
            String (*toString)(Test006);
            void (*init__Test006)(__Test006*);
            __Test006_VT() : 
                __isa(__Test006::__class()),
                __delete(&__rt::__delete<__Test006>),
                hashCode((int32_t(*)(Test006))&__Object::hashCode),
                equals((bool(*)(Test006, Object))&__Object::equals),
                getClass((Class(*)(Test006))&__Object::getClass),
                toString((String(*)(Test006))&__Object::toString),
                init__Test006(&__Test006::init__Test006) {
                }
        };


    }

}
