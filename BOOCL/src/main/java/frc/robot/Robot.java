/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.drive.*;
import edu.wpi.first.cameraserver.*;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
//22
//23
/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  double x;
  double y;
  // doubles for driving axes
  UsbCamera Cam;
  Joystick doIt = new Joystick(0);
  Talon frontLeft = new Talon(0);
  Talon frontRight = new Talon(1);
  Talon backLeft = new Talon(2);
  Talon backRight = new Talon(3);
  Talon pickupBoi = new Talon(4);
  Talon blaster = new Talon(5);
  Talon blasterSpin = new Talon(6);
  // Fix numbers. I'm a banana!
  SpeedControllerGroup left = new SpeedControllerGroup(frontLeft, backLeft);
  SpeedControllerGroup right = new SpeedControllerGroup(frontRight, backRight);
  DifferentialDrive driveyBoi = new DifferentialDrive(left, right);
  // Drivey things
  NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
  NetworkTableEntry tx = table.getEntry("tx"); // angle on x-axis form the crosshairs on the object to the origin
  NetworkTableEntry ty = table.getEntry("ty"); // angle on y-axis form the crosshairs on the object to the origin
  NetworkTableEntry ta = table.getEntry("ta"); // area of the object
  NetworkTableEntry tlong = table.getEntry("tlong"); // length of longest side
  NetworkTableEntry tshort = table.getEntry("tshort"); // length of shortest side
  NetworkTableEntry tvert = table.getEntry("tvert"); // vertical distance
  NetworkTableEntry thor = table.getEntry("thor"); // horizontal distance
  NetworkTableEntry getpipe = table.getEntry("getpipe"); // this tells us what "pipeline" we are on, basically different settings for the camera
  NetworkTableEntry ts = table.getEntry("ts"); // skew or rotation of target 

  double camx;
  double camy;
  double camarea;
  // Dooooooobles for lemonlite (:
  
  /*-----------nice-----------*/
  boolean aligned;
  boolean distanced;

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
    Cam = CameraServer.getInstance().startAutomaticCapture(0);
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  } 
  // ecin

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
      autoShoot();
        break;
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    x = doIt.getRawAxis(1);
    y = doIt.getRawAxis(0);
    driveyBoi.arcadeDrive(-x, y);

    if (doIt.getRawButton(6) == true) {
      autoShoot();
    }

    if(doIt.getRawButton(1) == true) {
      blaster.set(1);
      blasterSpin.set(-1);
    } else {
        blaster.set(0);
    }
    if (doIt.getRawButton(2) == true) {
      blasterSpin.set(1);
    }

    if (doIt.getRawButton(4) == true) {
      pickupBoi.set(1);
    } else {
      pickupBoi.set(0);
    }

    camx = tx.getDouble(0.0);
    camy = ty.getDouble(0.0);
    camarea = ta.getDouble(0.0);
    SmartDashboard.putNumber("LimelightX", camx);
    SmartDashboard.putNumber("LimelightX", camx);
    SmartDashboard.putNumber("LimelightArea", camarea);
    NetworkTableInstance.getDefault();
  
    SmartDashboard.putBoolean("Aligned", aligned);
    SmartDashboard.putBoolean("Distanced", aligned);
  
    SmartDashboard.putBoolean("Motor Safety", frontLeft.isSafetyEnabled());
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }

  // We need to fix the numbers for the range of vision
  public void autoShoot() {
    aligned = false;
    distanced = false;

    if (camx > 5 && camx < -5 && camy > 2.3 && camy < -2.3) {
      System.out.println("If you shoot you will fail and be sad.");
    }

    if (camx > 5) {
      left.set(-.3);
      right.set(-.3);
      aligned = false;
      // On left, twist right
    } else if (camx < -5) {
      left.set(.3);
      right.set(.3);
      aligned = false;
      // on right, twist left
    } else if (camx > -5 && camx < 5) {
      aligned = true;
    }

    if (camy > 2.3 && aligned == true) {
      left.set(.3);
      right.set(-.3);
      distanced = false;
      // Too close, backs up
    } else if (camy < -2.3 && aligned == true) {
      left.set(-.3);
      right.set(.3);
      distanced = false;
      // Too far, moves closer
    } else if (camy < 2.3 && camy > -2.3 && aligned == true) {
      distanced = true;
    }

    if (distanced == true) {
      System.out.println("Well boys we did it, unalignment is no more.");
      blaster.set(1);
      blasterSpin.set(-1);
      aligned = false;
      distanced = false;
    }
  }
}










































// nice





















































































































































// jude is kinda dum, not gonna lie