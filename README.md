# BrainControlledWheelchair
Code for CSU Brain Controlled Electric Wheelchair Senior Design project 2016/2017
Website: http://projects-web.engr.colostate.edu/ece-sr-design/AY16/wheelchair/index.html

To Update/Load Code Onto The Chair:
Export a runnable jar with motor controller as main for configurations titled BCW_MC.jar. Store this on motor controller Raspberry Pi.
(This should be configured to be called automatically when the Pi boots)

Export a runnable jar with app controller as main for configurations titled BCW_AC.jar. Store this on the touch screen Raspberry Pi.

To Run:
Navigate to BCW_AC.jar in the terminal on the Pi. 
Run with: sudo java -jar BCW_AC.jar

GUI will start.
