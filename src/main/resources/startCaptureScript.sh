#!/bin/bash

# Run the dumpcap program, redirecting it's standard error and output to null, and printing the PID to the standard output.

# You'll probably want to add a capture filter such as -f "ether proto 0x99B8" for Goose. Other useful EtherTypes: 0x99B9 for GSE, and 0x88BA for SV.

if [ -z "$2" ]; then
    nohup /usr/bin/dumpcap -i enp0s31f6 -w $1 &> /dev/null & echo $!
else
    nohup /usr/bin/dumpcap -f "$2" -i enp0s31f6 -w $1 &> /dev/null & echo $!
fi
