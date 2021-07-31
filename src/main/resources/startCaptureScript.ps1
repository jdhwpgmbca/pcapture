
Write-Output "Test Dumpcap" > $1.log
& 'C:\Program Files\Wireshark\dumpcap.exe' -i 'vEthernet' -w $1
Write-Output "Done."
