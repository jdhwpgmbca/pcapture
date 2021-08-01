$param1=$args[0]
$env:Path+= ";C:\Program Files\Wireshark"

# Start the dumpcap program in another window, writing the PID to the standard output.

# You'll probably want to add a capture filter such as -f ""ether proto 0x99B8" for Goose. Other useful EtherTypes: 0x99B9 for GSE, and 0x88BA for SV.

(Start-Process -FilePath "dumpcap.exe" -ArgumentList "-i vEthernet -w $param1" -PassThru).Id
