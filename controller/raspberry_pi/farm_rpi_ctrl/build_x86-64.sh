#!/bin/sh

mkdir -p  bin
cd bin || return 1
rm -rf *.*
cmake  -DGOAL="x86-64" ../src || return 1
make

