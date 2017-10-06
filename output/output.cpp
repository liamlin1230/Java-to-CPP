#pragma once

#include "java_lang.h"
#include "output.h"

#include <sstream>
#include <iostream>

using namespace java::lang;
using namespace inputs::test006;
using namespace std;


namespace __rt {

template<>
Class Array<String>::__class() {
  static java::lang::Class k =
    new java::lang::__Class(__rt::literal("[Ljava.lang.String"),
      __Object::__class(),
      __String::__class());

  return k;
}

template<>
template<>
Ptr<Class>::Ptr(const Ptr<Object>& other) {
  throw new ClassCastException();
}

template<>
template<>
Ptr<__A>::Ptr(const Ptr<Object>& other) {
  throw new ClassCastException();
}

template<>
template<>
Ptr<String>::Ptr(const Ptr<Object>& other) {
  throw new ClassCastException();
}

}


__A::__A() : __vptr(&vtable), fld(__rt::literal("A")) {
  this->__vptr->init__A(this);
}

void __A::setFld(A __this, String f) {
  __this->fld = f;
}

void __A::almostSetFld(A __this, String f) {
  String fld;
  fld = f;
}

String __A::getFld(A __this) {
  return __this->fld;
}

void __A::init__A(__A* __this) {
}

Class __A::__class() {
  return new __Class(  __rt::literal("inputs.test006.A"),   java::lang::__Object::__class());
}

__A_VT __A::vtable;


__Test006::__Test006() : __vptr(&vtable) {
  this->__vptr->init__Test006(this);
}

void __Test006::init__Test006(__Test006* __this) {
}

Class __Test006::__class() {
  return new __Class(  __rt::literal("inputs.test006.Test006"),   java::lang::__Object::__class());
}

__Test006_VT __Test006::vtable;


int main() {
  A a = new __A();
  a->__vptr->almostSetFld(a, __rt::literal("B"));
  cout << (a->__vptr->getFld(a)) << endl;
  a->__vptr->setFld(a, __rt::literal("B"));
  cout << (a->__vptr->getFld(a)) << endl;
}
