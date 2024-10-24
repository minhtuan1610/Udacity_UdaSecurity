package com.udacity.catpoint.security.service;


import com.udacity.catpoint.image.service.FakeImageService;
import com.udacity.catpoint.security.application.StatusListener;
import com.udacity.catpoint.security.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit test for simple App.
 */
@ExtendWith(MockitoExtension.class)
public class AppTest {

	private SecurityService securityService;

	private Sensor sensor;

	@Mock
	private StatusListener statusListener;

	@Mock
	private SecurityRepository securityRepository;

	@Mock
	private FakeImageService fakeImageService;

	@BeforeEach
	public void init() {
		securityService = new SecurityService(securityRepository, fakeImageService);
		sensor = new Sensor("1st Sensor", SensorType.DOOR);
	}

	/**
	 * Test 1: If alarm is armed and a sensor becomes activated, put the system into pending alarm status.
	 */
	@Test
	public void alarmStatus_armedAlarmActivatedSensor_statusPending() {
		// Ensure the arming status and alarm status always armed and no alarm
		when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
		when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
		securityService.changeSensorActivationStatus(sensor, true);
		// Confirm the conditions occur once.
		verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
	}

	/**
	 * Test 2: If alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm
	 */
	@Test
	public void alarmStatus_armedAlarmActivatedSensorPendingSystem_statusAlarm() {
		when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
		when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
		securityService.changeSensorActivationStatus(sensor, true);
		verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
	}

	/**
	 * Test 3: If pending alarm and all sensors are inactive, return to no alarm state.
	 */
	@Test
	public void alarmStatus_pendingAlarmInactivateAllSensor_noAlarmState() {
		when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
		sensor.setActive(false);
		securityService.changeSensorActivationStatus(sensor, false); // Need to update condition for this method
		verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
	}

	/**
	 * Test 4: If alarm is active, change in sensor state should not affect the alarm state.
	 */
	@Test
	public void alarmStatus_activateAlarmChangeSensorState_notAffectAlarmState() {
	}

	/**
	 * Test 5: If a sensor is activated while already active and the system is in pending state, change it to alarm state.
	 */
	@Test
	public void alarmStatus_activateTheActivatedSensorPendingSystem_changeToAlarmState() {
	}

	/**
	 * Test 6: If a sensor is deactivated while already inactive, make no changes to the alarm state.
	 */
	@Test
	public void alarmStatus_deactivateTheInactivatedSensor_noChangeAlarmState() {
	}

	/**
	 * Test 7: If the image service identifies an image containing a cat while the system is armed-home, put the system into alarm status.
	 */
	@Test
	public void alarmStatus_detectCatWhileArmedHomeSystem_statusAlarm() {
	}

	/**
	 * Test 8: If the image service identifies an image that does not contain a cat, change the status to no alarm as long as the sensors are not active.
	 */
	@Test
	public void alarmStatus_noCatIdentifyInactivatedSensor_noAlarmState() {
	}

	/**
	 * Test 9: If the system is disarmed, set the status to no alarm.
	 */
	@Test
	public void alarmStatus_disarmedSystem_noAlarmState() {
	}

	/**
	 * Test 10: If the system is armed, reset all sensors to inactive.
	 */
	@Test
	public void sensorStatus_disarmedSystem_deactivateAllSensor() {
	}

	/**
	 * Test 11: If the system is armed-home while the camera shows a cat, set the alarm status to alarm.
	 */
	@Test
	public void alarmStatus_armedSystemDetectedCat_statusAlarm() {
	}
}
