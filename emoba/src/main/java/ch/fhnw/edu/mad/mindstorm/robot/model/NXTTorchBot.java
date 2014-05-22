
/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.fhnw.edu.mad.mindstorm.robot.model;

import java.util.ArrayList;
import java.util.List;

import ch.fhnw.edu.mad.mindstorm.nxt.NXT;
import ch.fhnw.edu.mad.mindstorm.nxt.NXT.NXTActorPin;
import ch.fhnw.edu.mad.mindstorm.robot.motor.Actor;
import ch.fhnw.edu.mad.mindstorm.robot.sensor.Sensor;

/**
 * Robot derived from {@link NXTCastorBot}. It adds an actor on pin a to
 * drive motor to be able to grab objects.
 * 
 * @author Juerg Luthiger
 *
 */
public class NXTTorchBot extends NXTCastorBot {
	private int DIR = -1;
	public boolean isOpening = true;
	
	List<Sensor> sensors = new ArrayList<Sensor>();
	private Actor motorA;

	public NXTTorchBot(NXT nxt) {
		super(nxt);
	}
	
	@Override
	protected int getDirection() {
		return DIR;
	}

	@Override
	protected void createMotors() {
		super.createMotors();
		motorA = nxt.createActor(NXTActorPin.PIN_A);
	}

	/**
	 * Open/close gripper
	 */
	@Override
	public void action(boolean isStartAction) {
		if (isStartAction) {
			if (isOpening) {
				motorA.setSpeed(10);
				isOpening = false;
		    } else {
		    	motorA.setSpeed(-20);
		    	isOpening = true;
		    }
	    } else {
	    	 if (isOpening == true){
	    	    motorA.setSpeed(-10);
	    	 } else {
	    		 isOpening = false;
	    		 motorA.setSpeed(0);
	    	 }
	    }
	}
}

       



