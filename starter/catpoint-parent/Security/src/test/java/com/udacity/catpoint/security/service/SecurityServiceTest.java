package com.udacity.catpoint.security.service;

import com.udacity.catpoint.image.service.AwsImageService;
import com.udacity.catpoint.image.service.FakeImageService;
import com.udacity.catpoint.security.application.StatusListener;
import com.udacity.catpoint.security.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit test for simple App.
 */
@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

	private SecurityService securityService;

	private Sensor sensor;

	@Mock
	private StatusListener statusListener;

	@Mock
	private SecurityRepository securityRepository;

	@Mock
	private FakeImageService fakeImageService;

	@Mock
	private AwsImageService awsImageService;

	/**
	 * Method to create a list of sensors and add them into a Set.
	 * @param count the number of sensor in list
	 * @param status status of sensor
	 * @return the set of sensor
	 */
	private Set<Sensor> getAllSensors(int count, boolean status) {
		Set<Sensor> sensorsList = new HashSet<>();
		String randomName = UUID.randomUUID().toString();
		for (int i = 0; i < count; i++) {
			Sensor sensorRandom = new Sensor(randomName, SensorType.DOOR);
			sensorRandom.setActive(status);
			sensorsList.add(sensorRandom);
		}
		return sensorsList;
	}

	@BeforeEach
	public void init() {
		securityService = new SecurityService(securityRepository, fakeImageService);
		// The default active status: false
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
	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	public void alarmStatus_activateAlarmChangeSensorState_notAffectAlarmState(Boolean status) {
		when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
		securityService.changeSensorActivationStatus(sensor, status);
		// assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());
		verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
	}

	/**
	 * Test 5: If a sensor is activated while already active and the system is in pending state, change it to alarm state.
	 */
	@Test
	public void alarmStatus_activateTheActivatedSensorPendingSystem_changeToAlarmState() {
		when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
		sensor.setActive(true);
		securityService.changeSensorActivationStatus(sensor, true);
		verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
	}

	/**
	 * Test 6: If a sensor is deactivated while already inactive, make no changes to the alarm state.
	 */
	@ParameterizedTest
	@EnumSource(value = AlarmStatus.class, names = {"NO_ALARM", "PENDING_ALARM", "ALARM"})
	public void alarmStatus_deactivateTheInactivatedSensor_noChangeAlarmState(AlarmStatus status) {
		when(securityRepository.getAlarmStatus()).thenReturn(status);
		sensor.setActive(false);
		securityService.changeSensorActivationStatus(sensor, false);
		assertEquals(status, securityService.getAlarmStatus());
	}

	/**
	 * Test 7: If the image service identifies an image containing a cat while the system is armed-home, put the system into alarm status.
	 */
	@Test
	public void alarmStatus_detectCatWhileArmedHomeSystem_statusAlarm() {
		when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
		when(fakeImageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
		BufferedImage catImg = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
		securityService.processImage(catImg);
		verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
	}

	/**
	 * Test 8: If the image service identifies an image that does not contain a cat, change the status to no alarm as long as the sensors are not active.
	 */
	@Test
	public void alarmStatus_noCatIdentifyInactivatedSensor_noAlarmState() {
		Set<Sensor> sensors = getAllSensors(2, false);
		lenient().when(securityRepository.getSensors()).thenReturn(sensors); // Need to re-check again
		when(fakeImageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
		securityService.processImage(mock(BufferedImage.class));
		verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
	}

	/**
	 * Test 9: If the system is disarmed, set the status to no alarm.
	 */
	@Test
	public void alarmStatus_disarmedSystem_noAlarmState() {
		securityService.setArmingStatus(ArmingStatus.DISARMED);
		verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
	}

	/**
	 * Test 10: If the system is armed, reset all sensors to inactive.
	 * Tips: Sensors were not reset to inactive when the system was armed: put all sensors to the active state when disarmed,
	 * then put the system in the armed state; sensors should be inactivated.
	 */
	@ParameterizedTest
	@EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY", "ARMED_HOME"})
	public void sensorStatus_disarmedSystem_deactivateAllSensor(ArmingStatus status) {
		Set<Sensor> sensorSet = getAllSensors(2, true);
		when(securityRepository.getSensors()).thenReturn(sensorSet);
		when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
		securityService.setArmingStatus(status);
		for (Sensor sensorIndex : sensorSet) {
			assertEquals(false, sensorIndex.getActive());
		}
	}

	/**
	 * Test 11: If the system is armed-home while the camera shows a cat, set the alarm status to alarm.
	 * Tips: Put the system as disarmed, scan a picture until it detects a cat after that, make it armed, it should make the system in the ALARM state.
	 */
	@Test
	public void alarmStatus_armedSystemDetectedCat_statusAlarm() {
		// Put the system as disarmed
		when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
		// The camera shows a cat
		when(fakeImageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
		BufferedImage catImg = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
		securityService.processImage(catImg);

		securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
		verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
	}
}
