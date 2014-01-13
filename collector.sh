#!/bin/bash


while true; do

CPU_USAGE_PERCENT=`mpstat | awk '$12 ~ /[0-9.]+/ { print 100 - $12 }'`
 
MEM_USED_KB=`sar -r 1 1 | grep Average | awk '{print $3}'`
 
IO_READ_BYTE=`sar -b 1 1 | grep Average | awk '{print $5}'`
IO_WRITE_BYTE=`sar -b 1 1 | grep Average | awk '{print $6}'`

NW_TX_KBPS=`sar -n DEV 1 1 | grep eth | grep Average | awk '{print $6}'`

TIMESTAMP=`date +"%Y-%m-%dT%H:%M:%S%z"`

#echo "T03_VM_RICHIL"
#echo "Time: $(date)"
#echo "CPU_USAGE_PERCENT:" $CPU_USAGE_PERCENT 
#echo "MEM_USED_KB:" $MEM_USED_KB
#echo "MEM_USED_KB:" $MEM_USED_KB
#echo "IO_READ_BYTE:" $IO_READ_BYTE
#echo "IO_WRITE_BYTE:" $IO_WRITE_BYTE
#echo "NW_TX_KBPS:" $NW_TX_KBPS

echo {"\"vmname\"": "\"T03_VM_RICHIL\"", "\"cpu_usage_perc\"":$CPU_USAGE_PERCENT, "\"mem_used_kb\"":$MEM_USED_KB, "\"io_read_byte\"":$IO_READ_BYTE, "\"io_write_byte\"":$IO_WRITE_BYTE, "\"nw_tx_kbps\"":$NW_TX_KBPS} >> metric.log

done
