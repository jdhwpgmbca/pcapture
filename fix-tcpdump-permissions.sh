#!/bin/bash

chgrp tcpdump /usr/sbin/tcpdump
chmod a+x /usr/sbin/tcpdump
setcap cap_net_raw,cap_net_admin=eip /usr/sbin/tcpdump

echo "You must also add the user that needs to run tcpdump as a non-root user to the tcpdump group in /etc/group file."
