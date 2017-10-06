#!/bin/sh

sbt genCtags
jd-cmd lib/* -od decompiled
ctags --language-force=java -f.tags --extra=+q -a -R decompiled src
