#!/bin/bash

# Make sure the process id passed in $1 truly belongs to a dumpcap command,
# otherwise this could be a huge security hole!

all_dumpcaps=`ps -C dumpcap -o pid=`

if echo $all_dumpcaps | grep -w $1 > /dev/null; then
    kill -INT $1
fi
