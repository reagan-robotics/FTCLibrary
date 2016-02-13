package com.qualcomm.ftcrobotcontroller.opmodes;

import com.lasarobotics.library.drive.Tank;
import com.lasarobotics.library.nav.EncodedMotor;
import com.lasarobotics.library.nav.MotorInfo;
import com.lasarobotics.library.nav.PID;
import com.lasarobotics.library.util.Log;
import com.lasarobotics.library.util.MathUtil;
import com.lasarobotics.library.util.Units;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * Basic navigational test - driving forward and rotation
 */
public class BasicEncoderTest extends OpMode {
    private static final double WHEEL_RADIUS = 2;
    private static final Units.Distance WHEEL_RADIUS_UNIT = Units.Distance.INCHES;
    private static final double WHEEL_MECHANICAL_ADVANTAGE = 1;

    private static final double PID_P = 0.050;  //0.05
    private static final double PID_I = 0.000;  //0.01
    private static final double PID_D = 0.000;  //0.005
    private static final double DISTANCE = 1; //distance in feet
    private static final Units.Distance DISTANCE_UNIT = Units.Distance.FEET;
    private static final double MIN_POWER = 0;

    EncodedMotor frontLeft, frontRight, backLeft, backRight;
    PID pidLeft, pidRight;
    Log log;

    long lastTime = 0;
    long timeStart = 0;
    int phase = 0;

    private double coerce(double power) {
        return MathUtil.deadband(MIN_POWER, MathUtil.coerce(-1, 1, power));
    }

    @Override
    public void init() {
        log = new Log("/FTC_Logs/", "PID");

        frontLeft = new EncodedMotor(hardwareMap.dcMotor.get("lf"),
                new MotorInfo(WHEEL_RADIUS, WHEEL_RADIUS_UNIT, WHEEL_MECHANICAL_ADVANTAGE));
        frontRight = new EncodedMotor(hardwareMap.dcMotor.get("rf"),
                new MotorInfo(WHEEL_RADIUS, WHEEL_RADIUS_UNIT, WHEEL_MECHANICAL_ADVANTAGE));
        backLeft = new EncodedMotor(hardwareMap.dcMotor.get("lb"),
                new MotorInfo(WHEEL_RADIUS, WHEEL_RADIUS_UNIT, WHEEL_MECHANICAL_ADVANTAGE)); //set wheel radius for distance calculations
        backRight = new EncodedMotor(hardwareMap.dcMotor.get("rb"),
                new MotorInfo(WHEEL_RADIUS, WHEEL_RADIUS_UNIT, WHEEL_MECHANICAL_ADVANTAGE));

        backLeft.setDirection(DcMotor.Direction.FORWARD);
        frontLeft.setDirection(DcMotor.Direction.FORWARD);
        backRight.setDirection(DcMotor.Direction.REVERSE);
        frontRight.setDirection(DcMotor.Direction.REVERSE);

        /*backLeft.setTargetPosition(DISTANCE, DISTANCE_UNIT);
        backRight.setTargetPosition(DISTANCE, DISTANCE_UNIT);
        frontLeft.setTargetPosition(DISTANCE, DISTANCE_UNIT);
        frontRight.setTargetPosition(DISTANCE, DISTANCE_UNIT);*/

        //Create PID looper
        pidLeft = new PID();
        pidLeft.setSetpoint(Units.Distance.convertToAngle(DISTANCE,
                WHEEL_RADIUS / WHEEL_MECHANICAL_ADVANTAGE,
                WHEEL_RADIUS_UNIT, DISTANCE_UNIT, Units.Angle.ENCODER_COUNTS));
        //pidLeft.setMaxAcceleration(PID_MAX_ACCEL);
        pidLeft.setCoefficients(PID_P, PID_I, PID_D);

        pidRight = new PID();
        pidRight.setSetpoint(Units.Distance.convertToAngle(DISTANCE,
                WHEEL_RADIUS / WHEEL_MECHANICAL_ADVANTAGE,
                WHEEL_RADIUS_UNIT, DISTANCE_UNIT, Units.Angle.ENCODER_COUNTS));
        //pidRight.setMaxAcceleration(PID_MAX_ACCEL);
        pidRight.setCoefficients(PID_P, PID_I, PID_D);
    }

    @Override
    public void init_loop() {
        lastTime = System.nanoTime();
        timeStart = lastTime;
        phase = 0;
    }

    @Override
    public void loop() {
        switch (phase) {
            case 0:
                long time = System.nanoTime();
                double timeDelta = (time - lastTime) / 1000000000.0;

                double leftPos = Math.abs(backLeft.getCurrentPosition()) +
                        Math.abs(frontLeft.getCurrentPosition());
                double rightPos = Math.abs(backRight.getCurrentPosition()) +
                        Math.abs(frontRight.getCurrentPosition());

                telemetry.addData("Pos", leftPos + ", " + rightPos);

                pidLeft.addMeasurement(leftPos, timeDelta);
                pidRight.addMeasurement(rightPos, timeDelta);

                telemetry.addData("PID", pidLeft.getOutputValue() + ", " + pidRight.getOutputValue());

                double power = (pidLeft.getOutputValue() + pidRight.getOutputValue()) / 2;
                //double powerCompensation = 0;

                telemetry.addData("PID Power", power);
                log.add("Time", time - timeStart);
                log.add("PID Power", power);
                log.add("Position Left", leftPos);

                double left = power;
                double right = power;

                left = coerce(left);
                right = coerce(right);

                telemetry.addData("Power", left + ", " + right);
                Tank.motor4(frontLeft, frontRight, backLeft, backRight, left, right);

                if (left == 0 && right == 0)
                    phase = 1;
                return;
            case 1:
                backLeft.setPowerFloat();
                frontLeft.setPowerFloat();
                backRight.setPowerFloat();
                frontRight.setPowerFloat();
                return;
        }
    }

    @Override
    public void stop() {
        log.saveAs(Log.FileType.CSV);
    }
}
