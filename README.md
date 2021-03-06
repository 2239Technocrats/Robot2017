# Robot 2018
FIRST team 2239's robot code for 2018

# Controller Documentation
Required imports
--  
`edu.wpi.first.wpilibj.XboxController;` Initalizing and using Xbox Controllers  
`edu.wpi.first.wpilibj.Joystick;` Initalizing and using Joysticks  

Initalizing a Controller
--  
`public XboxController controller = new XboxController(int controllerPort);`  
`public Joystick controller = new Joystick(int controllerPort);`  
Initalizes either a XboxController or a Joystick and gives it the port controllerPort.

Buttons
--
`controllerClass.getRawButton(int button);`  
Returns a boolean, true if button on controllerClass has button pushed.  
**Button values (XboxController)**  
1 - A  
2 - B  
3 - X  
4 - Y  
5 - LB  
6 - RB  
7 - Back  
8 - Start  
9 - Left Thumbstick  
10 -  Right Thumbstick  
  
Axis (Joysticks, Triggers)
--
`controllerClass.getRawAxis(int which);`  
Returns a double between -1 and 1 for joysticks and 0 to 1 for triggers representing the current position of said axis. Which represents what axis to check.  
**Axis Values (XboxController)**  
0 - Left Stick X  
1 - Left Stick Y  
2 - Left Trigger  
3 - Right Trigger  
4 - Right Stick X  
5 - Right Stick Y
  
POV Positions (D-Pad)
-- 
`controllerClass.getPOV(int pov);`  
Retruns a boolean, true if the POV of controllerClass is in the position defined by int pov.  
**Position values (XboxController)**  
0 - Idle  
1 - Up  
2 - Up-Right  
3 - Right  
4 - Down-Right  
5 - Down  
6 - Down-Left  
7 - Left  
8 - Left-Up  
  
