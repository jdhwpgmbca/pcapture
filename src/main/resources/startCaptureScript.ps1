$param1=$args[0]
$env:Path+= ";C:\Program Files\Wireshark"
(Start-Process -FilePath "dumpcap.exe" -ArgumentList "-i vEthernet -w $param1" -PassThru).Id
