#!/bin/bash

chgrp wireshark /usr/bin/dumpcap
chmod a+x /usr/bin/dumpcap
setcap cap_net_raw,cap_net_admin=eip /usr/bin/dumpcap

echo "You must also add the user that needs to run dumpcap as a non-root user to the wireshark group in /etc/group file."
