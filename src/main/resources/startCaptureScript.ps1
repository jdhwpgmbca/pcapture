$out=$args[0]
$ethernet=$args[1]
$filter=$args[2]
$env:Path+= ";C:\Program Files\Wireshark"

# Start the dumpcap program in another window, writing the PID to the standard output.

# You'll probably want to add a capture filter such as -f ""ether proto 0x99B8" for Goose. Other useful EtherTypes: 0x99B9 for GSE, and 0x88BA for SV.
# You'll also likely need to rename vEthernetBridge to whatever network interface you're using on Windows. It's likely that you'll have several if you're
# using Docker or HyperV.

if ($($args.Count) -eq 3) {
    (Start-Process -FilePath "dumpcap.exe" -ArgumentList "-i", "`"$ethernet`"", "-f", "`"$filter`"", "-w", "$out" -PassThru).Id
} else {
    (Start-Process -FilePath "dumpcap.exe" -ArgumentList "-i", "`"$ethernet`"", "-w", "$out" -PassThru).Id
}
