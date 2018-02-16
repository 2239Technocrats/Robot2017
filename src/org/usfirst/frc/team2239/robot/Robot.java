package org.usfirst.frc.team2239.robot;

import java.util.ArrayList;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DriverStation;

/*
 * Jess controls:
 * Left on arrows should do 180 degree turn
 * Right on arrows should be cancel autonomous
 */

//TODO double check for double imports
//TODO check for unused imports (don't just delete them; think about if what was using them is missing)
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
//import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	private static final double NORMAL_POWER_LEVEL = 0.8;
	private static final double AUTONOMOUS_POWER_LEVEL = 0.7;
	private static final double TICKS_PER_INCH = 81.5;
	private static final double FORWARD_ONE = 130;
	private static final boolean GOING_TO_GO_FOR_QUEST = true;
	// true is left false is right

	// NetworkTable table;
	// NetworkTable contoursTable;
	// NetworkTable blobsTable;
	final double halfYFov = Math.toRadians(32.5) / 2.0; // half the vertical
														// field of vision
														// (radians)
														// //fiddleable (you can
														// change this value for
														// calibration)
	final double halfXFov = Math.toRadians(60) / 2.0; // half the horizontal
														// field of vision
														// (radians)
														// //fiddleable
	final double realTapeHeight = 5; // height of the strip of tape (inches)
	final double spread = 8.25; // distance between the centers of the strips of
								// tape (inches)
	final double pixelScreenHeight = 480; // height of screen (pixels)
	final double pixelScreenWidth = 640; // width of screen (pixels)
	final double away = 12; // how many inches away from the peg you want the
							// robot to move to before its charge. Determines
							// target.

	public static Robot instance;
	// from
	// http://wpilib.screenstepslive.com/s/4485/m/26401/l/255419-choosing-an-autonomous-program-from-smartdashboard

	private boolean field_data = false;
	private boolean goingForMiddle = true;
	public Timer timer; // Timer
	public XboxController controller; // Control for the robot
	public Solenoid gearShift;
	// public Compressor myCompressor;
	// public PowerDistributionPanel myPDP;
	public GearStateMachine autoGear;
	// public String defaultAutoName = "middle";

	int autoStep = 0; // 0 if autonomous has not been planned, 1 if planned but
						// not done, 2 if done
	double autoVolts = 0;
	double maxAutoVolts = .8;
	// double testAngle = 0;
	// double testDistance = 0;
	// double testTheta = 0;

	/*
	 * FRC default code - keep here for now final String defaultAuto =
	 * "Default"; final String customAuto = "My Auto"; String autoSelected;
	 * SendableChooser<String> chooser = new SendableChooser<>();
	 */

	AHRS navSensor; // The navigation sensor object
	int toggleAmt = 3; // how many different buttons are toggling
	boolean[] toggleReadys = new boolean[toggleAmt]; // {speedToggleReady,
														// gearToggleReady,
														// turnToggle}
	boolean gearOpen = false;
	Action curAction = null;
	String gameData;

	double speed = 1;
	AccelerationHelper baseline;
	// drive = new TechnoDrive(4,1,3,2);//small bot
	// TODO make sure you change this for SpiderBot
	// void MotorGroupLeft(leftMotor1 leftMotor2 leftMotor3);
	// MotorGroupRight(rightMotor2, rightMotor2, rightMotor3);

	WPI_TalonSRX leftFollowerMotor2 = new WPI_TalonSRX(5);
	WPI_TalonSRX leftFollowerMotor1 = new WPI_TalonSRX(3);
	WPI_TalonSRX rightFollowerMotor2 = new WPI_TalonSRX(6);
	WPI_TalonSRX rightFollowerMotor1 = new WPI_TalonSRX(4);
	WPI_TalonSRX leftLeaderMotor = new WPI_TalonSRX(1);
	WPI_TalonSRX rightLeaderMotor = new WPI_TalonSRX(2);
	WPI_TalonSRX liftWheelsRight = new WPI_TalonSRX(20);
	WPI_TalonSRX liftWheelsLeft = new WPI_TalonSRX(21);
	WPI_TalonSRX lift = new WPI_TalonSRX(22);
	WPI_TalonSRX rampDeploy = new WPI_TalonSRX(23);

	// 3s are old lead motors
	SpeedControllerGroup left = new SpeedControllerGroup(leftLeaderMotor, leftFollowerMotor1, leftFollowerMotor2);
	SpeedControllerGroup right = new SpeedControllerGroup(rightLeaderMotor, rightFollowerMotor1, rightFollowerMotor2);
	SpeedControllerGroup liftWheels = new SpeedControllerGroup(liftWheelsLeft, liftWheelsRight);
	WPI_TalonSRX[] encoderMotors = new WPI_TalonSRX[] { leftFollowerMotor1 };

	// TODO fix just testing
	TechnoDrive drive = new TechnoDrive(left, right); // class that handles
														// basic drive
														// operations
	Boolean open = true;
	private int location;

	// This is the constructor. Whenever a new Robot object is made
	// this is the function that will be called to make it
	// public Robot() {
	// contoursTable = NetworkTable.getTable("GRIP/myContoursReport");
	// blobsTable = NetworkTable.getTable("GRIP/myBlobsReport");
	// }

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		System.out.println("Robot has started init!");

		// CameraServer.getInstance().startAutomaticCapture();

		// Default all the "ready"s to true. No buttons should be pressed at the
		// start, therefore all should be ready to be pressed.
		for (int i = 0; i < toggleReadys.length; i++) {
			toggleReadys[i] = true;
		}

		gameData = DriverStation.getInstance().getGameSpecificMessage();
		location = DriverStation.getInstance().getLocation();

		// myCompressor = new Compressor(10);
		// myCompressor.setClosedLoopControl(true);

		// gearShift = new Solenoid(CAN ID on dashboard, channel on PCM (what's
		// it plugged into));
		gearShift = new Solenoid(8, 0);
		// gearShift = new Solenoid(7, 0); //practicebot
		gearShift.set(!open); // TODO figure out if false means closed or open
		timer = new Timer();
		controller = new XboxController(0);

		// myPDP = new PowerDistributionPanel();
		// myPDP.getVoltage();
		try {
			navSensor = new AHRS(
					SPI.Port.kMXP); /*
									 * Alternatives: SerialPort.Port.kMXP,
									 * I2C.Port.kMXP or SerialPort.Port.kUSB
									 */
			System.out.println("NavSensor = " + navSensor);
		} catch (RuntimeException ex) {
			DriverStation.reportError("Error instantiating navX-MXP: " + ex.getMessage(), true);
		}

		// "movement name", inches
		// 9'6'' = 114'' from baseline to wall
		// Robot is about 32''
		// Want to travel 114-32+4
		// Want it to be 4173 ticks

		SmartDashboard.putNumber("Forwards1", 192);
		SmartDashboard.putNumber("Forwards2", 84);
		SmartDashboard.putNumber("Forwards3", 12);
		SmartDashboard.putNumber("Backwards1", 10);
		SmartDashboard.putNumber("Turn1", 90);
		SmartDashboard.putNumber("Turn2", 60);
		// SmartDashboard.putString("Auto", defaultAutoName);

		// makeMotorsUseEncoders(encoderMotors);
		// initFollower(rightLeaderMotor, rightFollowerMotor1);
		// initFollower(rightLeaderMotor, rightFollowerMotor2);
		// initFollower(leftLeaderMotor, leftFollowerMotor1);
		// initFollower(leftLeaderMotor, leftFollowerMotor2);

		/*
		 * Because the lift wheels run in opposite directions, we need to invert one of them
		 */
		liftWheelsLeft.setInverted(true);
		
		
		for (int i = 0; i < encoderMotors.length; i++) {
			encoderMotors[i].setSelectedSensorPosition(0, EncoderAccelerator.ENCODER_CLOSED_LOOP_PRIMARY, 100);
		}

		autoGear = new GearStateMachine(drive, navSensor, encoderMotors);

		System.out.println("Robot has finished init");
	}

	// public void initFollower(TalonSRX leader, TalonSRX follower) {
	// follower.changeControlMode(TalonControlMode.Follower);
	// follower.set(leader.getDeviceID());
	// }
	// leader and slave code

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the
	 * switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	public Action[] getScenarioTestTurn() {
		Action[] actions = new Action[1];
		actions[0] = new RotationAccelerator(drive, navSensor, -90, NORMAL_POWER_LEVEL);
		return actions;
	}

	public Action[] getScenarioTestDrive() {
		Action[] actions = new Action[1];
		actions[0] = new EncoderAccelerator(drive, encoderMotors, 120, NORMAL_POWER_LEVEL);
		return actions;
	}

	public Action[] getScenarioLeftClear() {
		Action[] actions = new Action[2];
		actions[0] = new EncoderAccelerator(drive, encoderMotors, 84, NORMAL_POWER_LEVEL);
		// actions[1] = new RotationAccelerator(drive, navSensor,
		// SmartDashboard.getNumber("Turn1", 0), NORMAL_POWER_LEVEL);
		return actions;
	}

	public Action[] getScenarioLeftGet() {

		Action[] actions = new Action[4];
		// actions.add(new RotationAccelerator(drive, navSensor,
		// SmartDashboard.getNumber("Turn1", 0), NORMAL_POWER_LEVEL));
		// actions[0] = new RotationAccelerator(drive, navSensor,
		// SmartDashboard.getNumber("Turn1", 0), NORMAL_POWER_LEVEL);
		actions[0] = new EncoderAccelerator(drive, encoderMotors, 120, NORMAL_POWER_LEVEL);
		actions[1] = new RotationAccelerator(drive, navSensor, 90, NORMAL_POWER_LEVEL);
		// actions[3] = new EncoderAccelerator(drive, encoderMotors,
		// SmartDashboard.getNumber("Forwards1", 0), NORMAL_POWER_LEVEL);
		// TODO add raise lift command
		// TODO add open lift command
		// return (Action[]) actions.toArray();
		return actions;
	}

	public Action[] getScenarioRightClear() {
		Action[] actions = new Action[2];
		actions[0] = new EncoderAccelerator(drive, encoderMotors, 120, NORMAL_POWER_LEVEL);
		// actions[1] = new RotationAccelerator(drive, navSensor, -1 *
		// SmartDashboard.getNumber("Turn1", 0), NORMAL_POWER_LEVEL);
		return actions;
	}

	public Action[] getScenarioRightGet() {
		Action[] actions = new Action[4];
		// actions[0] = new EncoderAccelerator(drive, encoderMotors,
		// SmartDashboard.getNumber("Forwards2", 0), NORMAL_POWER_LEVEL);
		actions[0] = new EncoderAccelerator(drive, encoderMotors, 120, NORMAL_POWER_LEVEL);
		actions[1] = new RotationAccelerator(drive, navSensor, -1 * 90, NORMAL_POWER_LEVEL);
		// TODO add raise lift command
		// TODO add open lift command
		return actions;
	}

	public Action[] getScenarioMiddleLeftClear() {
		Action[] actions = new Action[4];
		// TODO add in new angles for turn
		actions[0] = new EncoderAccelerator(drive, encoderMotors, 84, NORMAL_POWER_LEVEL);
		actions[1] = new RotationAccelerator(drive, navSensor, -45, NORMAL_POWER_LEVEL);
		actions[2] = new EncoderAccelerator(drive, encoderMotors, 4.4, NORMAL_POWER_LEVEL);
		actions[3] = new RotationAccelerator(drive, navSensor, 45, NORMAL_POWER_LEVEL);
		return actions;
	}

	public Action[] getScenarioMiddleRightClear() {
		Action[] actions = new Action[4];
		// TODO add in new angles for turn
		actions[0] = new EncoderAccelerator(drive, encoderMotors, 120, NORMAL_POWER_LEVEL);
		actions[1] = new RotationAccelerator(drive, navSensor, 45, NORMAL_POWER_LEVEL);
		actions[2] = new EncoderAccelerator(drive, encoderMotors, 120, NORMAL_POWER_LEVEL);
		actions[3] = new RotationAccelerator(drive, navSensor, -45, NORMAL_POWER_LEVEL);
		return actions;
	}

	public Action[] getScenarioMiddleGetLeft() {
		ArrayList<Action> actionList = new ArrayList<Action>();
		// Action[] actions = new Action[1];
		actionList.add(new EncoderAccelerator(drive, encoderMotors, 84, NORMAL_POWER_LEVEL));
		actionList.add(new RotationAccelerator(drive, navSensor, -60, NORMAL_POWER_LEVEL));
		actionList.add(new EncoderAccelerator(drive, encoderMotors, 60, NORMAL_POWER_LEVEL));
		actionList.add(new RotationAccelerator(drive, navSensor, 90, NORMAL_POWER_LEVEL));
		actionList.add(new RotationAccelerator(drive, navSensor, 60, NORMAL_POWER_LEVEL));
		// TODO add raise lift command
		// TODO add open lift command
		return actionList.toArray(new Action[actionList.size()]);
		// return actions;
	}

	public Action[] getScenarioMiddleGetRight() {
		ArrayList<Action> actionList = new ArrayList<Action>();
		// Action[] actions = new Action[1];
		actionList.add(new EncoderAccelerator(drive, encoderMotors, 84, NORMAL_POWER_LEVEL));
		actionList.add(new RotationAccelerator(drive, navSensor, 60, NORMAL_POWER_LEVEL));
		actionList.add(new EncoderAccelerator(drive, encoderMotors, 60, NORMAL_POWER_LEVEL));
		actionList.add(new RotationAccelerator(drive, navSensor, -90, NORMAL_POWER_LEVEL));
		actionList.add(new RotationAccelerator(drive, navSensor, -60, NORMAL_POWER_LEVEL));
		// TODO add raise lift command
		// TODO add open lift command
		return actionList.toArray(new Action[actionList.size()]);
		// return actions;

	}

	@Override
	public void autonomousInit() {

		/*
		 * default FRC code; leave it for now autoSelected =
		 * chooser.getSelected(); autoSelected =
		 * SmartDashboard.getString("Auto Selector", defaultAuto);
		 * System.out.println("Auto selected: " + autoSelected);
		 */

		navSensor.reset();
		for (int i = 0; i < encoderMotors.length; i++) {
			encoderMotors[i].setSelectedSensorPosition(0, EncoderAccelerator.ENCODER_CLOSED_LOOP_PRIMARY, 100);
		}
		gameData = DriverStation.getInstance().getGameSpecificMessage();
		location = DriverStation.getInstance().getLocation();

		// reset nav sensor position to zero

		// TechnoDrive theRobot;
		// double startTime;
		// double distance;
		// double maxVelocity;
		// myCompressor.start();
		// 53.5 ticks per inch old conversion

		// diameter/tickrate*pi= .0122718463 inches per tick
		// ticks per inch*(diameter*pi/4)=0.26987199534
		// ticks per inch

		// TICKS_PER_INCH

		// String auto = SmartDashboard.getString("Auto", defaultAutoName);
		// Encoder: TechnoDrive driveTrain, TalonSRX[] motorsToLookAt, double
		// distance, double maxVelocity
		// Rotation: TechnoDrive driveTrain, AHRS navSensor, double turnAngle,
		// double maxVelocity
		// double forwardsMaxVolts = NORMAL_POWER_LEVEL;
		// double backwardsMaxVolts = NORMAL_POWER_LEVEL;
		// double turnMaxVolts = NORMAL_POWER_LEVEL;
		// .8 = 80% power

		// autoVolts = 0;
		// maxAutoVolts = AUTO_POWER_LEVEL;
		// drive at 70% speed

		// autoGear.futureActions = getScenarioTestDrive();
		// autoGear.futureActions = getScenarioTestTurn();

		if (gameData.length() > 0) {
			if (gameData.charAt(0) == 'L') {
				field_data = true;
			} else {
				field_data = false;
			}
		}

		System.out.println("starting if startments with location=" + location + ", field_data=" + field_data);
		if (location == 3) {
			System.out.println("driver location: " + location);
			if (field_data == true) {
				System.out.println("trying to do rightClear");
				autoGear.futureActions = getScenarioRightClear();
			} else {
				System.out.println("trying to do rightGet");
				autoGear.futureActions = getScenarioRightGet();
			}
		} else if (location == 1) {
			System.out.println("driver location: " + location);

			if (field_data == true) {
				System.out.println("trying to do LeftGet");
				autoGear.futureActions = getScenarioLeftGet();
			} else {
				System.out.println("trying to do LeftCear");
				autoGear.futureActions = getScenarioLeftClear();
			}

		} else if (location == 2 && goingForMiddle == true) {
			if (field_data == true) {
				System.out.println("goingForMidLeft is running");
				autoGear.futureActions = getScenarioMiddleGetLeft();
			} else {
				System.out.println("goingForMidRight is running");
				autoGear.futureActions = getScenarioMiddleGetRight();
			}
		} else if (location == 2 && goingForMiddle != true) {
			if (field_data == true) {
				System.out.println("clearingMidRight is running");
				autoGear.futureActions = getScenarioMiddleRightClear();
			} else {
				System.out.println("clearingMidLeft is running");
				autoGear.futureActions = getScenarioMiddleLeftClear();
			}
		}
		curAction = null;
	}

	/**
	 * This function is called periodically during autonomous
	 */

	@Override
	public void autonomousPeriodic() {
		if (curAction == null) {
			curAction = autoGear.getNextAction();
			if (curAction == null) {
				return;
			}
		}
		runAction(false);
	}

	// Called in between the end of autonomous and the start of teleop
	@Override
	public void teleopInit() {
		speed = 1;
		// return speed to max
		curAction = null;
	}

	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {
		// System.out.println("Running teleop periodic!");
		// if (curAction == null) {
		// System.out.println("auto accelerator is null");
		// if no action, set accelerator to null
		// }

		// System.out.println("How fast the rearRightMotor is going:
		// "+rearRightMotor.getEncVelocity());
		// System.out.println("How far the rearRightMotor has gone:
		// "+rearRightMotor.getEncPosition());
		//
		// System.out.println("How fast the rearLeftMotor is going:
		// "+rearLeftMotor.getEncVelocity());
		// System.out.println("How far the rearLeftMotor has gone:
		// "+rearLeftMotor.getEncPosition());
		//
		// SmartDashboard.putBoolean("Trigger", toggleReadys[0]); //TODO
		// uncomment

		boolean[] triggers = new boolean[toggleAmt];
		// Go through each toggle
		// Set all the values in triggers appropriately.
		for (int index = 0; index < toggleAmt; index++) {
			// 0: Speed changing
			// 1: Gear
			// 2: Auto turn

			// see if the buttons are pushed down or not
			switch (index) {
			case 0:
				triggers[index] = controller.getTrigger(XboxController.Hand.kLeft)
						|| controller.getTrigger(XboxController.Hand.kRight);
				break;
			case 1:
				triggers[index] = controller.getRawButton(5) || controller.getRawButton(6);
				break;
			case 2:
				triggers[index] = controller.getSimplePOV() == 6;
				break;
			default:
				break; // we are no longer using this toggle button
			}

			// actually update/run
			boolean isReady = toggleReadys[index]; // is this button ready to be
													// activated/pressed down
			boolean isTriggered = triggers[index];
			if (isReady) {
				if (isTriggered) { // button is down and this is the first time
									// I've noticed
					// fire the trigger; the button has been pressed!
					switch (index) {
					case 0:
						if (speed == 1) {
							speed = .7;
						} else {
							speed = 1;
						}
						break;

					case 1:
						if (gearOpen) {
							// TODO if these are changed, make sure the pipes
							// are switched on SpiderBot
							gearShift.set(!open); // close it //TODO make sure
													// these are accurate
							gearOpen = !open;
						} else {
							gearShift.set(open); // open it
							gearOpen = open;
						}
						// System.out.println("we have/lost a box" + gearOpen);
						// // TODO
						// delete
						break;
					//
					// case 2:
					// System.out.println("Starting a new rotation!");
					// double turnAngle = 180;
					// //public RotationAccelerationHelper (TechnoDrive
					// driveTrain, AHRS navSensor, double turnAngle, double
					// maxVelocity)
					// curAction = new RotationAccelerator(drive, navSensor,
					// turnAngle, .8);
					// break;
					//

					default:
						break; // we are no longer using this toggle button

					}
					toggleReadys[index] = false; // Don't notice it anymore
													// until the button is
													// lifted up
				}
			} else { // if not ready
				if (!isTriggered) { // button is no longer up (or just isn't up)
					toggleReadys[index] = true; // I'm ready for it to be pushed
												// down again
				}
			}
		}

		SmartDashboard.putBoolean("Turn is triggered", triggers[2]);

		runAction();

		if (controller.getRawButton(1)) {
			// Button A = push cube
			liftWheels.set(1);
		} else {
			liftWheels.set(0);
		}
		if (controller.getRawButton(2)) {
			// Button B = pull cube
			liftWheels.set(-1);
		} else {
			liftWheels.set(0);
		}
		if (controller.getRawButton(3)) {
			// Button X = lift up
			lift.set(1);
		} else {
			lift.set(0);
		}
		if (controller.getRawButton(4)) {
			// Button Y = lift down
			lift.set(-1);
		} else {
			lift.set(0);
		}
		if (controller.getRawButton(8)) {
			// Start Button = lower ramp
			rampDeploy.set(1);
		} else {
			lift.set(0);
		}
		if (controller.getRawButton(7)) {
			// Back Button = raise ramp
			rampDeploy.set(-1);
		} else {
			rampDeploy.set(0);
		}

		// Up=0, up-right = 1, right = 2. Goes to 7.
		int POV = controller.getSimplePOV();
		System.out.print(POV);
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
	}

	public void disabledInit() {
		curAction = null;
	}

	// //TODO set values to 0 to start match
	// public void makeMotorsUseEncoders(TalonSRX[] motors) {
	// for (TalonSRX motor : motors) {
	// motor.setFeedbackDevice(FeedbackDevice.QuadEncoder); //Set the feedback
	// device that is hooked up to the talon
	// motor.setPID(0.5, 0.001, 0.0); //Set the PID constants (p, i, d)
	// motor.enableControl(); //Enable PID control on the talon
	// //Notes from Trent
	// //p helps you get there if you're not getting there
	// //d helps you limit oscillation
	// //f is feed forward which gives you a push
	// //i you don't really use much
	// }
	// }

	public void runAction(boolean teleop) {
		// System.out.println("POV: " + controller.getSimplePOV());
		if (curAction != null) {
			if (teleop && controller.getSimplePOV() == 2) { // If the button
															// that cancels the
															// turn is pressed
															// then cancel the
															// turn.
				curAction = null;
			} else {
				// System.out.println("I'm going to move autonomously!"); // If
				// we
				// don't
				// tell
				// the
				// robot
				// not
				// to
				// turn,
				// it
				// turns.
				// This
				// isn't
				// rocket
				// science.
				boolean doneYet;
				// System.out.println("Running accel in runAccelerator()");
				doneYet = curAction.run();
				if (doneYet) {
					// System.out.println("Accelerator has finished. Stopping
					// it.");
					curAction = null;
				} else {
					// System.out.println("Accelerator not finished yet.");
				}
			}
		} else if (teleop) {
			drive.tankDrive(controller, speed);
		}
	}

	public void runAction() {
		runAction(true);
	}

	// everything below is vision code

	// get the requested values from the contours table posted by GRIP
	// public double[][] getDataFromGRIPContours(String[] propertiesToGet) {
	// double[][] ans = new double[propertiesToGet.length][];
	// for (int i=0; i<propertiesToGet.length; i++) {
	// String property = propertiesToGet[i];
	// System.out.print("Getting new data!!");
	// double[] propertyArray = contoursTable.getNumberArray(property, new
	// double[0]);
	// System.out.print("Got a propertyArray "+property+": ");
	// for (double value : propertyArray) {
	// System.out.print(" value: ");
	// System.out.print(value);
	// }
	// ans[i] = propertyArray;
	// System.out.println();
	// }
	// return ans;
	// }

	// get the values we can from blobs
	// This is a workaround since GRIP wasn't giving us all the values we wanted
	// public double[][] getDataFromGRIPBlobs(String[] propertiesToGet) {
	// double[][] ans = new double[propertiesToGet.length][];
	// for (int i=0; i<propertiesToGet.length; i++) {
	// String property = propertiesToGet[i];
	// double[] propertyArray = blobsTable.getNumberArray(property, new
	// double[0]);
	// System.out.print("Got a propertyArray "+property+": ");
	// for (double value : propertyArray) {
	// System.out.print(" value: ");
	// System.out.print(value);
	// }
	// ans[i] = propertyArray;
	// System.out.println();
	// }
	// return ans;
	// }

	// Take all the data from GRIP and return the tape contours.
	// This is where finding the tape (and ignoring other things, including the
	// peg covering the tape) takes place.
	public Contour[] getContours(double[][] contourPropertyArrays) {
		if (contourPropertyArrays[0].length < 2)
			return new Contour[0]; // If there aren't even two contours to look
									// at, return empty

		// The class/final result is called "Contours" even though it has some
		// info from blobsTable and some from contoursTable
		Contour[] contours = new Contour[contourPropertyArrays[0].length];
		System.out.println("How many contours are seen: " + contours.length);
		try {
			for (int i = 0; i < contours.length; i++) { // for each contour
				Contour contour = new Contour(); // create default Contour with
													// all values at default
													// value
				contour.x = contourPropertyArrays[0][i];
				contour.y = contourPropertyArrays[1][i];
				contour.w = contourPropertyArrays[2][i];
				contour.h = contourPropertyArrays[3][i];
				contour.area = contour.w * contour.h;

				/*
				 * //only use if the contours fail contour.w =
				 * contourPropertyArrays[0][i]; contour.area =
				 * contourPropertyArrays[1][i]; contour.x =
				 * blobPropertyArrays[0][i]; contour.y =
				 * blobPropertyArrays[1][i]; contour.h = contour.area/contour.w;
				 */
				contours[i] = contour; // make the new contour and add it
			}

			// 2 inches wide
			// 5 inches tall
			// So, ratio should be 5/2
			double targetRatio = 5.0 / 2.0;
			double scores[] = new double[contours.length];
			for (int i = 0; i < contours.length; i++) {
				double score = 0;
				Contour contour = contours[i];
				if (contour.area > 100) {
					double ratio = contour.h / contour.w;
					score = Math.abs(ratio - targetRatio);
					System.out.println("Score: " + score);
				} else {
					score = 1000; // Give it a super high score so it won't be
									// chosen
				}

				scores[i] = score;
			}

			int[] indexes = getIndexesOfSmallestTwoNums(scores);
			for (int i = 0; i < indexes.length; i++) {
				if (scores[indexes[i]] >= 1000) { // if either of the scores is
													// really bad, return empty
													// list
					return new Contour[0];
				}
			}
			contours = new Contour[] { contours[indexes[0]], contours[indexes[1]] };

			return contours;
		}

		catch (ArrayIndexOutOfBoundsException exc) {
			System.out.println("Caught the killer error! About to return.");
			return new Contour[0];
		}
	}

	public double getLargestNum(double[] myArr) {
		double largest = myArr[0];
		for (double num : myArr) {
			if (largest < num) {
				largest = num;
			}
		}
		return largest;
	}

	public int[] getIndexesOfLargestTwoNums(double[] myArr) {
		if (myArr.length < 2) { // if myArr has less than two doubles in it,
								// just return an empty array.
			return new int[2];
		}

		int index1 = 0;
		int index2 = 1;
		double bigNum1 = myArr[0];
		double bigNum2 = myArr[1];

		for (int i = 2; i < myArr.length; i++) {
			double num = myArr[i];
			if (num > bigNum1 || num > bigNum2) {
				if (bigNum1 > bigNum2) { // bigNum2 is smaller so replace it
					bigNum2 = num;
					index2 = i;
				} else { // replace bigNum1
					bigNum1 = num;
					index1 = i;
				}
			}
		}
		return new int[] { index1, index2 };
	}

	public int[] getIndexesOfSmallestTwoNums(double[] myArr) {
		if (myArr.length < 2) { // if myArr has less than two doubles in it,
								// just return an empty array.
			return new int[2];
		}

		int index1 = 0;
		int index2 = 1;
		double smallNum1 = myArr[0];
		double smallNum2 = myArr[1];

		for (int i = 2; i < myArr.length; i++) {
			double num = myArr[i];
			if (num < smallNum1 || num < smallNum2) {
				if (smallNum1 < smallNum2) { // bigNum2 is smaller so replace it
					smallNum2 = num;
					index2 = i;
				} else { // replace bigNum1
					smallNum1 = num;
					index1 = i;
				}
			}
		}
		return new int[] { index1, index2 };
	}

}
