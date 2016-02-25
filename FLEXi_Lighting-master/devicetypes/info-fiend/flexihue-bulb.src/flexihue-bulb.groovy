/**
 *  FLEXiHue Hue Bulb Device Type 
 *
 *  Version 1.1  (2015-5-5)
 *
 *  Author: Anthony Pastor
 */
 
// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "FLEXiHue Bulb", namespace: "info_fiend", author: "Anthony Pastor") {
		capability "Switch Level"
		capability "Actuator"
		capability "Color Control"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        capability "Test Capability"


        attribute "sceneLevel", "number"
        attribute "sceneHue", "number"
        attribute "sceneSat", "number"
        attribute "myBackColor", "string"
		attribute "sceneSwitch", "string"
        attribute "sceneName", "string"
        attribute "offTime", "number"

		command "setAdjustedColor"      
		command "setOffTime"
		command "setScName"
		command "backgrounderValue"       
        command "saveScene"
        command "setScSwitch"
        command "sceneManual"
        command "sceneFree"
        command "sceneSlave"
        command "sceneMaster"
        command "sceneNone"
        command "setLevelFromThing"
        command "setHueFromThing"
        command "setSatFromThing"        
	}

	simulator {
		// TODO: define status and reply messages here
	}

// NORMAL CONTROLLERS
	standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
		state "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor: "#79b821"
		state "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor: "#ffffff"
	} 
	standardTile("refresh", "device.switch", decoration: "flat") {
		state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
	}
	controlTile("rgbSelector", "device.color", "color", height: 3, width: 3) {
		state "color", action:"setAdjustedColor"
	}
	controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2) {
		state "level", action:"setLevelFromThing"
	}
	valueTile("level", "device.level", decoration: "flat") {
		state "level", label: 'Level ${currentValue}%'
	}
	controlTile("saturationSliderControl", "device.saturation", "slider", height: 1, width: 2) {
		state "saturation", action:"setSatFromThing"
	}
	valueTile("saturation", "device.saturation", decoration: "flat") {
		state "saturation", label: 'Sat ${currentValue}    '
	}
	controlTile("hueSliderControl", "device.hue", "slider", height: 1, width: 2) {
		state "hue", action:"setHueFromThing"
	}
	valueTile("hue", "device.hue") {
		state "hue", label: 'Hue ${currentValue}',
	        backgroundColors:[
                [value: 1, color: "#FFA500"],
                [value: 15, color: "#FAF600"],
                [value: 20, color: "#F2E7A0"],
                [value: 39, color: "#76F58D"],  
                [value: 44, color: "#CEF0DA"],
                [value: 50, color: "#FAF7F7"],                 
                [value: 59, color: "#BDE5F2"],
                [value: 70, color: "#0000FF"],
                [value: 75, color: "#A000FC"],                
                [value: 83, color: "#F505F5"],
                [value: 98, color: "#FF0000"]
            ]    
	} 


// SCENE TILES
    valueTile("sceneName", "device.sceneName", decoration: "flat") {
		state "sceneName", label: 'Scene: ${currentValue}'
	}

    valueTile("offTime", "device.offTime") {
		state "offTime", label: 'OffTime: ${currentValue}'
	}

	standardTile("sceneSwitch", "device.sceneSwitch", width: 1, height: 1, canChangeIcon: true, decoration: "flat", defaultState: "Manual") {
        state "Manual", label: 'Manual', icon:"https://dl.dropboxusercontent.com/u/2403292/STIcons/manualSettings-large.png"
        state "Master", label: '${name}', icon:"https://dl.dropboxusercontent.com/u/2403292/STIcons/MCP-large.png" 
        state "Slave", label: 'Slave', action:"sceneFree", icon:"https://dl.dropboxusercontent.com/u/2403292/STIcons/slave-large.png", nextState: "Freebie"
		state "Freebie", action:"sceneSlave", icon:"https://dl.dropboxusercontent.com/u/2403292/STIcons/free-large.png", nextState: "Slave"

        }


// SCENE CONTROLLERS
	controlTile("sceneLevelSliderControl", "device.sceneLevel", "slider", height: 1, width: 2) {
		state "sceneLevel", action: "setScLevel"
	}
    
	controlTile("sceneHueSliderControl", "device.sceneHue", "slider", height: 1, width: 2) {
		state "sceneHue", action: "setScHue"
	}

	controlTile("sceneSatSliderControl", "device.sceneSat", "slider", height: 1, width: 2) {
		state "sceneSat", action: "setScSat"
	}

	main(["switch"])
	details(["switch", "offTime", "sceneSwitch", "level", "levelSliderControl", "hue", "hueSliderControl", "saturation", "saturationSliderControl", "refresh"])      

}

// parse events into attributes
def parse(description) {
	log.debug "parse() - $description"
	def results = []

	def map = description
	if (description instanceof String)  {
		log.debug "FLEXiHue Bulb stringToMap - ${map}"
		map = stringToMap(description)
	}

	if (map?.name && map?.value) {
		results << createEvent(name: "${map?.name}", value: "${map?.value}")
	}

	results

}


// handle commands

def setScSwitch(String inState) {
	log.debug "FLEXiHue(${device.label}): Executing 'setScSwitch(${inState})'"
	sendEvent (name: "sceneSwitch", value: inState, isStateChange: true)
        	          
}

def sceneManual() {
//	log.debug "FLEXiHue: sceneManual setting sceneSwitch to: MANUAL."  

    def curLevel = device.currentValue("level")
    setScLevel(curLevel)
    def curSat = device.currentValue("saturation")
    setScSat(curSat)
    def curHue = device.currentValue("hue")
    setScHue(curHue)

	def newValue = "Manual" as String
    setScSwitch(newValue)

}

def sceneFree() {
//	log.debug "FLEXiHue: sceneFree setting sceneSwitch to: FREEEEEE."
	def newValue = "Freebie" as String
   	setScSwitch(newValue)
	refresh()    
}

def sceneSlave() {
//	log.debug "FLEXiHue: sceneSlave setting sceneSwitch to: SLAVE."  
	def newValue = "Slave" as String
 	setScSwitch(newValue)
	refresh()
}

def saveScene(inValue, String inMode, Number inOffTime) {

	log.debug "FLEXiHue(${device.label}: saveScene:"

	log.debug "sceneName to: ${inMode}"
	sendEvent(name: "sceneName", value: inMode, isStateChange: true)
    	

    if (inOffTime == null || inOffTime == 0) {
		log.debug "Setting offTime to: default 30 b/c inOffTime is null or equal to 0."
	    sendEvent(name: "offTime", value: 30, isStateChange: true)
    } else {
		log.debug "Setting offTime to: ${inOffTime}"
	    sendEvent(name: "offTime", value: inOffTime, isStateChange: true)
    }	
		
    sendEvent(name: "sceneHue", value: inValue.hue)  
    log.debug "Setting sceneHue to: ${inValue.hue}."
	sendEvent(name: "sceneSat", value: inValue.saturation)
    log.debug "Setting sceneSat to: ${inValue.saturation}."

	sendEvent(name: "sceneLevel", value: inValue.level)
    log.debug "Setting sceneLevel to: ${inValue.level}."        

}


def on() {

	on(3)   		// sets default TransitionTime = 3 seconds

}

def on(Number inTransitionTime) {

	log.debug "FLEXiHue(${device.label}): 'On(transitionTime)'"

	def theLevel = device.currentValue("sceneLevel")
    log.debug "current sceneLevel value is ${theLevel}."
    if(theLevel == null) {
    	theLevel = device.currentValue("level")
        log.debug "no sceneLevel value, so use current level value of ${theLevel}."
        if (theLevel == null) {
        	theLevel = 99
            log.debug "no sceneLevel or level values, so theLevel defaults to ${theLevel}."
        }
    }
    log.trace "...sending to 'setLevel(${theLevel})' with TT."
	setLevel(theLevel, inTransitionTime) 
}

def off() {

	off(2)  		// sets default Off TransitionTime = 2 seconds    
    
}

def off(Number inTransitionTime) {

	log.debug "FLEXiHue(${device.label}): Executing 'Off(transitionTime)'"
    if (device.currentValue("switch") == "on" ) {
		sendEvent(name: "switch", value: "off", isStateChange: true)
    }
	parent.off(this, inTransitionTime)
        
}

def poll() {

	parent.poll(this)
}

def nextLevel() {

	def level = device.currentValue("level") as Integer ?: 0
	if (level < 100) {
		level = Math.min(25 * (Math.round(level / 25) + 1), 100) as Integer
	}
	else {
		level = 25
	}
	setLevel(level, 3)
}

def setLevel(Number percent) {

	def transitiontime = 3
	log.trace "...sending to 'setLevel' with TT."
    setLevel(percent, transitiontime)

}

def setLevel(Number percent, transitiontime) {

	log.debug "FLEXiHue(${device.label}): Executing 'setLevel(${percent}, ${transitiontime})'."

	sendEvent(name: "switch", value: "on", isStateChange: true)    
	sendEvent(name: "level", value: percent, isStateChange: true)

	log.trace "Calling 'parent.setLevel(${this}, ${percent}, ${transitiontime})'."
	parent.setLevel(this, percent, transitiontime)
}

def setLevelFromThing(Number percent) {

	log.debug "FLEXiHue: setLevelfromThing setting sceneSwitch to: Manual."  
	def newValue = "Manual" as String
	log.trace "...calling 'setScSwitch(${newValue})'."    
    setScSwitch(newValue)

	log.trace "...sending to 'setLevel(${percent}, 3)'."
	setLevel(percent, 3)
    
}


def setSaturation(Number percent) {

	def transitiontime = 3
    log.trace "...sending to 'setSaturation(${percent}, ${transitiontime})'."
	setSaturation(percent, transitiontime) 
}


def setSaturation(Number percent, transitiontime) {

	log.debug "FLEXiHue(${device.label}): Executing 'setSaturation(${percent}, ${transitiontime})'"
    
	log.trace "Calling 'parent.setSaturation(${this}, ${percent}, ${transitiontime})'."    
	parent.setSaturation(this, percent, transitiontime)
	sendEvent(name: "saturation", value: percent, isStateChange: true)

}

def setSatFromThing(Number percent) {

	log.debug "FLEXiHue: setSatfromThing setting sceneSwitch to: Manual."  
	def newValue = "Manual" as String
	log.trace "...calling 'setScSwitch(${newValue})'."    
    setScSwitch(newValue)
	
    log.trace "...sending to 'setSaturation(${percent}, 3)'."
	setSaturation(percent, 3)
}


def setHue(Number percent) {

	def transitiontime = 3
    log.trace "...sending to 'setHue(${percent}, ${transitiontime})'."
	setHue(percent, transitiontime)
    
}

def setHue(Number percent, transitiontime) 
{
	log.debug "FLEXiHue(${device.label}): Executing 'setHue(${percent}, ${transitiontime})'."

	log.trace "Calling 'parent.setHue(${this}, ${percent}, ${transitiontime})'."    
	parent.setHue(this, percent, transitiontime)
	sendEvent(name: "hue", value: percent, isStateChange: true)
    
}

def setHueFromThing(Number percent) {

	log.debug "FLEXiHue: setHuefromThing setting sceneSwitch to: Manual."  
	def newValue = "Manual" as String
	log.trace "...calling 'setScSwitch(${newValue})'."
	setScSwitch(newValue)

	log.trace "...sending to 'setHue(${percent}, 3)'."
	setHue(percent, 3)
}


def setColor(inValue) {

	log.debug "FLEXiHue(${device.label}): setColor: ${inValue}"
	def curSWvalue = device.currentValue("switch")
	log.debug "switch value is currently ${curSWvalue}."
    
	if (!inValue.transitiontime) {
		inValue << [transitiontime: 3]
	}
    
//    if (inValue.switch == null) {
//		inValue << [switch: "on"]
//	}
    
	if (inValue.hex) {
    
		sendEvent(name: "color", value: inValue.hex)
        
	} 
       
    
    log.debug "Calling 'parent.setColor (${inValue})."    
  
	parent.setColor(this, inValue)

	curSWvalue = device.currentValue("switch")
    log.debug "curSWvalue is now ${curSWvalue}...sending events."
	
	sendEvent(name: "hue", value: inValue.hue)
    sendEvent(name: "saturation", value: inValue.saturation)
	sendEvent(name: "level", value: inValue.level, isStateChange: true)
	sendEvent(name: "switch", value: "on", isStateChange: true)
    
	curSWvalue = device.currentValue("switch")
    log.debug "Final curSWvalue is ${curSWvalue}."

}


def setAdjustedColor(value) {

	log.debug "FLEXiHue: setAdjustedColor: ${value}"
	def adjusted = value + [:]
	adjusted.hue = adjustOutgoingHue(value.hue)
	adjusted.level = null // needed because color picker always sends 100
	setColor(adjusted)
}


def save() {

	log.debug "FLEXiHue: Executing 'save'"
    
}

def refresh() {

	log.debug "FLEXiHue: Executing 'refresh'"
	parent.poll()
    
}

def adjustOutgoingHue(percent) {
	def adjusted = percent
	if (percent > 31) {
		if (percent < 63.0) {
			adjusted = percent + (7 * (percent -30 ) / 32)
		}
		else if (percent < 73.0) {
			adjusted = 69 + (5 * (percent - 62) / 10)
		}
		else {
			adjusted = percent + (2 * (100 - percent) / 28)
		}
	}
	log.info "FLEXiHue: percent: $percent, adjusted: $adjusted"
	adjusted
}