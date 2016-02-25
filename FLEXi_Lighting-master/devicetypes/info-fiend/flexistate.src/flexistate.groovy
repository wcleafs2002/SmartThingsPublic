/**
 *  FLEXiState Virtual Switch
 *
 *  Copyright 2015 Anthony Pastor
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "flexistate", namespace: "info_fiend", author: "Anthony Pastor") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
     
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
		state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
		state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
		state "turningOn", label:'${name}', icon:"st.switches.switch.on", backgroundColor:"#79b821"
		state "turningOff", label:'${name}', icon:"st.switches.switch.off", backgroundColor:"#ffffff"
		}
		main "button"
		details "button"
	}
}

def parse(String description) {
}

def on() {
	sendEvent(name: "switch", value: "on", isStateChange: true)
}

def off() {
	sendEvent(name: "switch", value: "off", isStateChange: true)
}