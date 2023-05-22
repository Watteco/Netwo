# Netw'o

Install this app  via Google Play Store : https://play.google.com/store/apps/details?id=watteco.netwo&hl=fr&gl=US

Connect to your Netw'o device.
And have a look at the graph to check the current network quality.

BLE Serial Interface
--------------------

After each linkCheckReq the Netwo send on the serial BLE:
"
  \n
  TX <SentFrame>/<FrameToSend>\n
"
  
After reception of linkCheckAns the Netwo send on the serial BLE:
"
  <nbReceivedFrame>/<nbSentFrame>\n
  Gateway : %d \n
  TX Margin : %d\n
  TX SF :%d \n
  RX SNR: %d \n
  RX RSSI :%d \n
  RX Window:%d\n
  RX SF:%d\n
  RX Delay:%d \n
  Battery:%d\nNetId: %d [option if battery is always measured]  
  \n\n
"
    
    
After sent all frame the netwo send on the serial BLE:
"
    \n
    \n********RESULT**********\n
    <ReceivedFrame>/<SentFrame-1>\nPER: <ReceivedFrame*100/(SentFrame-1)>\n
    Average GATEWAY:%d \n
    Average MARGIN:%d \n
    Average SNR:%d \n
    Average RSSI:%d \n
    ********RESULT**********\n\n\n
"
   
    
Command to interact with the Netwo:
  S<nbFrameToSend>,<SF>,<ADR>\n  =>send <nbFrameToSend> frame (maximum 99) on spcipfied <SF> (from 7 to 12) with <ADR> (1: set ADR)
  X => Stop the sending of frame.
  MDEVEUI? => ask the actual devEUI
  MAPPEUI? => ask the actual appEUI
  MDEVEUI<x> => modify the actual devEUI. <x> from 0 to 2. 0:  70b3d5e75e0xxxxx 1: 70b3d5e75e1xxxxx 2: 70b3d5e75e3xxxxx 
  RST => reboot the netwo.
  OFF => switch OFF the netwo.
  
 
