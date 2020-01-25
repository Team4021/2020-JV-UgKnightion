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
  NetworkTableEntry tx = table.getEntry("tx");
  NetworkTableEntry ty = table.getEntry("ty");
  NetworkTableEntry ta = table.getEntry("ta");
  NetworkTableEntry tlong = table.getEntry("tlong");
  NetworkTableEntry tshort = table.getEntry("tshort");
  NetworkTableEntry tvert = table.getEntry("tvert");
  NetworkTableEntry thor = table.getEntry("thor");
  NetworkTableEntry getpipe = table.getEntry("getpipe");
  NetworkTableEntry ts = table.getEntry("ts");
  
  double camx;
  double camy;
  double camarea;

  boolean aligned;
  boolean distanced;

  double x;
  double y;

  VictorSP Fr = new VictorSP(2);
  VictorSP Fl = new VictorSP(8);
  VictorSP Br = new VictorSP(3);
  VictorSP Bl = new VictorSP(4);

  SpeedControllerGroup left = new SpeedControllerGroup(Fl, Bl);
  SpeedControllerGroup right = new SpeedControllerGroup(Fr, Br);
  DifferentialDrive goBoi = new DifferentialDrive(left, right);

  Joystick doIt = new Joystick(0);

  Timer time =  new Timer();



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
      goBoi.arcadeDrive(1, 0);
      time.delay(6);
      goBoi.arcadeDrive(0, 0);
      time.delay(20);
        // Put default auto code here
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
    goBoi.arcadeDrive(-x, y);

    camx = tx.getDouble(0.0);
    camy = ty.getDouble(0.0);
    camarea = ta.getDouble(0.0);
    SmartDashboard.putNumber("limelightx", camx);
    SmartDashboard.putNumber("limelighty", camy);
    SmartDashboard.putNumber("limelightarea", camarea);
    NetworkTableInstance.getDefault();

    SmartDashboard.putBoolean("Aligned", aligned);
    SmartDashboard.putBoolean("distanced", distanced);

    SmartDashboard.putBoolean("Motor Safety", Fl.isSafetyEnabled());

    if (doIt.getRawButton(6) == true) {
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
      System.out.println("Stop Shooty Shoot");
    }

    if (doIt.getRawButton(6) && camx > 5) {
      left.set(.3);
      right.set(.3);
      aligned = false;
    } else if (doIt.getRawButton(6) && camx < -5) {
      left.set(-.3);
      right.set(-.3);
      aligned = false;
    } else if (doIt.getRawButton(6) && camx > -5 && camx <5) {
      aligned = true;
    }

    if (doIt.getRawButton(6) && camy > 2.3 && aligned == true) {
      left.set(-.3);
      right.set(.3);
      distanced = false;
    } else if (doIt.getRawButton(6) && camy < -2.3  && aligned == true) {
      left.set(.3);
      right.set(-.3);
      distanced = false;
    } else if (doIt.getRawButton(6) && camy < 2.3 && camy > -2.3 && aligned == true) {
      distanced = true;
    }

    if (doIt.getRawButton(6) && distanced == true && aligned == true) {
      System.out.println("Do the shoot");
      //blaster.set(1);
      aligned = false;
      distanced = false;
    }
  }
}
