#!/bin/sh

mkdir -p  bin
cd bin || return 1
rm -rf *.*
cmake -DGOAL="pi" ../src || return 1
make

