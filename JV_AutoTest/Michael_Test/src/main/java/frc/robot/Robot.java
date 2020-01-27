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
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.drive.*;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Encoder;





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

  boolean aligned;
  boolean distanced;

  double x;
  double y;

  Joystick bidoof = new Joystick(0);

  VictorSP frontRight = new VictorSP(2);
  VictorSP frontLeft = new VictorSP(8);
  VictorSP backRight = new VictorSP(3);
  VictorSP backLeft = new VictorSP(4);

  Timer timer = new Timer();
  Encoder countyBoi = new Encoder(9, 10);
  // I have no idea what these numbers mean
  
  SpeedControllerGroup left = new SpeedControllerGroup(frontLeft, backLeft);
  SpeedControllerGroup right = new SpeedControllerGroup(frontRight, backRight);
  DifferentialDrive bigMoveyBoi = new DifferentialDrive(left, right);


  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
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
      bigMoveyBoi.arcadeDrive(.5, 0);
      timer.delay(6);
      bigMoveyBoi.arcadeDrive(0, 0);
      timer.delay(20);
        // Put default auto code here
        break;
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    x = bidoof.getRawAxis(1);
    y = bidoof.getRawAxis(0);
    bigMoveyBoi.arcadeDrive(-x, y);

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

    if (bidoof.getRawButton(6) == true) {
      autoShoot();
    } else {
      aligned = false;
      distanced = false;
    }
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }

  public void autoShoot() {
    aligned = false;
    distanced = false;

    if (camx > 5 && camx < -5 && camy > 2.3 && camy < -2.3) {
      System.out.println("No shoot boi");
    }

    if (bidoof.getRawButton(6) && camx > 5) {
      left.set(.3);
      right.set(.3);
      aligned = false;
      // On left, twist right
    } else if (bidoof.getRawButton(6) && camx < -5) {
      left.set(-.3);
      right.set(-.3);
      aligned = false;
      // on right, twist left
    } else if (bidoof.getRawButton(6) && camx > -5 && camx < 5) {
      aligned = true;
    }

    if (bidoof.getRawButton(6) && camy > 2.3 && aligned == true) {
      left.set(-.3);
      right.set(.3);
      distanced = false;
      // Too close, backs up
    } else if (bidoof.getRawButton(6) && camy < -2.3 && aligned == true) {
      left.set(.3);
      right.set(-.3);
      distanced = false;
      // Too far, moves closer
    } else if (bidoof.getRawButton(6) && camy < 2.3 && camy > -2.3 && aligned == true) {
      distanced = true;
    }

    if (bidoof.getRawButton(6) && distanced == true && aligned == true) {
      System.out.println("We did it boys, unalignment is no more");
      //blaster.set(1)
      aligned = false;
      distanced = false;
    }
  }
}
