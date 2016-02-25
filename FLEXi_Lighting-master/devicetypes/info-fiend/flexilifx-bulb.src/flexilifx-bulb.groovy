/**
 *  FLEXi-Lifx Bulb
 *
 *  based on Nicolas Cerveaux' LiFX (Connect) app
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
    definition (name: "FLEXiLIFX Bulb", namespace: "info_fiend", author: "anthony pastor") {
        capability "Polling"
        capability "Switch"
        capability "Switch Level"
        capability "Color Control"
        capability "Refresh"
        capability "Test Capability"

        attribute "sceneLevel", "number"
        attribute "sceneHue", "number"
        attribute "sceneSat", "number"
        attribute "myBackColor", "string"
		attribute "sceneSwitch", "string"
        attribute "sceneName", "string"
        attribute "offTime", "number"

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
        command "setAdjustedColor"
    }

    simulator {
    }

    tiles {
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "on", label:'${name}', action:"switch.off", icon:"st.Lighting.light14", backgroundColor:"#79b821", nextState:"turningOff"
            state "off", label:'${name}', action:"switch.on", icon:"st.Lighting.light14", backgroundColor:"#ffffff", nextState:"turningOn"
            state "turningOn", label:'${name}', icon:"st.Lighting.light14", backgroundColor:"#79b821"
            state "turningOff", label:'${name}', icon:"st.Lighting.light14", backgroundColor:"#ffffff"
        }
        controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false) {
            state "level", action:"switch level.setLevel"
        }
        controlTile("rgbSelector", "device.color", "color", height: 3, width: 3, inactiveLabel: false) {
            state "color", action:"setAdjustedColor"
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
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
}

private debug(data){
    if(parent.appSettings.debug == "true"){
        log.debug(data)
    }
}

private getAccessToken() {
    return parent.appSettings.accessToken;
}

private sendCommand(path, method="GET", body=null) {
    def accessToken = getAccessToken()
    def pollParams = [
        uri: "https://api.lifx.com:443",
        path: "/v1beta1/"+path+".json",
        headers: ["Content-Type": "application/x-www-form-urlencoded", "Authorization": "Bearer ${accessToken}"],
        body: body
    ]
    debug(method+" Http Params ("+pollParams+")")
    
    try{
        if(method=="GET"){
            httpGet(pollParams) { resp ->            
                parseResponse(resp)
            }
        }else if(method=="PUT") {
            httpPut(pollParams) { resp ->            
                parseResponse(resp)
            }
        }
    } catch(Exception e){
        debug("___exception: " + e)
    }
}

private parseResponse(resp) {
    debug("Response: "+resp.data)
    if(resp.status == 200) {
        if (resp.data) {
            if(resp.data.power){
                def brightness = Math.ceil(resp.data.brightness*100)
                def hue = Math.ceil(resp.data.color.hue / 3.6)
                def saturation = Math.ceil(resp.data.color.saturation*100)
                
                //update switch
                if(device.currentValue("switch")!=resp.data.power){
                    debug("Update switch to "+resp.data.power)
                    sendEvent(name: "switch", value: resp.data.power)
                }
                
                // update level
                if(brightness != device.currentValue("level")){
                    debug('Update level to '+brightness)
                    sendEvent(name: 'level', value: brightness)
                }
                
                // update hue
                if(hue != device.currentValue("hue")){
                    debug('Update hue to '+hue)
                    sendEvent(name: 'hue', value: hue)
                }
                
                // update saturation
                if(saturation != device.currentValue("saturation")){
                    debug('Update saturation to '+saturation)
                    sendEvent(name: 'saturation', value: saturation)
                }
            }
        }
    }else if(resp.status == 201){
        debug("Something was created")
    }
}

//parse events into attributes
def parse(value) {
    debug("Parsing '${value}' for ${device.deviceNetworkId}")
}




// FLEXi Commands

def setScSwitch(String inState) {
	log.debug "FLEXiLiFX(${device.label}): Executing 'setScSwitch(${inState})'"
	sendEvent (name: "sceneSwitch", value: inState, isStateChange: true)
        	          
}

def sceneManual() {
//	log.debug "FLEXiLiFX: sceneManual setting sceneSwitch to: MANUAL."  

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
//	log.debug "FLEXiLiFX: sceneFree setting sceneSwitch to: FREEEEEE."
	def newValue = "Freebie" as String
   	setScSwitch(newValue)
	refresh()    
}

def sceneSlave() {
//	log.debug "FLEXiLiFX: sceneSlave setting sceneSwitch to: SLAVE."  
	def newValue = "Slave" as String
 	setScSwitch(newValue)
	refresh()
}

def saveScene(inValue, String inMode, Number inOffTime) {

	log.debug "FLEXiLiFX(${device.label}: saveScene:"

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





private sendAdjustedColor(data, transitiontime) {
    def hue = Math.ceil(data.hue*3.6)
    def saturation = data.saturation/100
    def brightness = data.level/100
    def power = data.power
    
    sendCommand("lights/"+device.deviceNetworkId+"/color", "PUT", 'color=hue%3A'+hue+'%20saturation%3A'+saturation+'%20brightness%3A'+brightness+'&duration='+transitiontime+'&power_on='+power)
}


def setColor(inValue) {
	
    log.debug "FLEXiLiFX(${device.label}): setAdjustedColor: ${inValue}"
	
    def curSWvalue = device.currentValue("switch")
	log.debug "switch value is currently ${curSWvalue}."
    
	if (!inValue.transitiontime) {
		inValue << [transitiontime: 1]
	}
    
//    if (inValue.switch == null) {
//		inValue << [switch: "on"]
//	}
    
	def data = [:]
    data.hue = inValue.hue
    data.saturation = inValue.saturation
    data.level = inValue.level

	data.power = inValue.switch
    
    if (data.power == "on") {
    	data.power = "true"
    }    

	def transitiontime = inValue.transitiontime 
    
    sendAdjustedColor(data, transitiontime)


    
	curSWvalue = device.currentValue("switch")
    log.debug "curSWvalue is now ${curSWvalue}...sending events."
	
	sendEvent(name: "hue", value: inValue.hue)
    sendEvent(name: "saturation", value: inValue.saturation)
	sendEvent(name: "level", value: inValue.level, isStateChange: true)
	sendEvent(name: "switch", value: "on", isStateChange: true)
    
	curSWvalue = device.currentValue("switch")
    log.debug "Final curSWvalue is ${curSWvalue}."

}

def setLevel(double percent) {

	def transitiontime = 1
    log.trace "...sending to 'setLevel(${percent}, ${transitiontime})'."
	
/**    def data = [:]
    data.hue = device.currentValue("hue")
    data.saturation = device.currentValue("saturation")
**/

    setLevel(percent, transitiontime)

}

def setLevel(double percent, transitiontime) {

	log.debug "FLEXiLiFX(${device.label}): Executing 'setLevel(${percent} with TT.)'."


	
/**    def data = [:]
    data.hue = device.currentValue("hue")
    data.saturation = device.currentValue("saturation")
**/

	def brightness = ( percent / 100 ) as Double
    sendCommand("lights/"+device.deviceNetworkId+"/color", "PUT", 'brightness%3A'+brightness+'&duration='+transitiontime+'&power_on=true')
  
	sendEvent(name: "switch", value: "on", isStateChange: true)    
	sendEvent(name: "level", value: percent, isStateChange: true)


}

def setLevelFromThing(Number percent) {

	log.debug "FLEXiLiFX: setLevelfromThing setting sceneSwitch to: Manual."  
	def newValue = "Manual" as String
	log.trace "...calling 'setScSwitch(${newValue})'."    
    setScSwitch(newValue)

	log.trace "...sending to 'setLevel(${percent} with TT)'."
	setLevel(percent, 1)
    
}

def setSaturation(Number percent) {

	def transitiontime = 1
    log.trace "...sending to 'setSaturation(${percent}, ${transitiontime})'."
	setSaturation(percent, transitiontime) 
}


def setSaturation(Number percent, transitiontime) {

	log.debug "FLEXiLiFX(${device.label}): Executing 'setSaturation(${percent}, ${transitiontime})'"
    
	def saturation = ( percent / 100 ) as Double
    sendCommand("lights/"+device.deviceNetworkId+"/color", "PUT", 'saturation%3A'+saturation+'&duration='+transitiontime+'&power_on=false')
	sendEvent(name: "saturation", value: percent, isStateChange: true)

}

def setSatFromThing(Number percent) {

	log.debug "FLEXiLiFX: setSatfromThing setting sceneSwitch to: Manual."  
	def newValue = "Manual" as String
	log.trace "...calling 'setScSwitch(${newValue})'."    
    setScSwitch(newValue)
	
    log.trace "...sending to 'setSaturation(${percent}, 1)'."
	setSaturation(percent, 1)
}


def setHue(Number percent) {

	def transitiontime = 1
    log.trace "...sending to 'setHue(${percent}, ${transitiontime})'."
	setHue(percent, transitiontime)
    
}

def setHue(Number percent, transitiontime) 
{
	log.debug "FLEXiLiFX${device.label}): Executing 'setHue(${percent}, ${transitiontime})'."

 	def hue = Math.ceil(percent*3.6)
	
    sendCommand("lights/"+device.deviceNetworkId+"/color", "PUT", 'hue%3A'+hue+'&duration='+transitiontime+'&power_on=false')


	sendEvent(name: "hue", value: percent, isStateChange: true)
    
}

def setHueFromThing(Number percent) {

	log.debug "FLEXiLiFX: setHuefromThing setting sceneSwitch to: Manual."  
	def newValue = "Manual" as String
	log.trace "...calling 'setScSwitch(${newValue})'."
	setScSwitch(newValue)

	log.trace "...sending to 'setHue(${percent}, 1)'."
	setHue(percent, 1)
}



def on() {

	log.debug "FLEXiLiFX(${device.label}): 'On()'"

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
    log.trace "...sending to 'setLevel(${theLevel})'."
	setLevel(theLevel) 	

//    sendCommand("lights/"+device.deviceNetworkId+"/power", "PUT", "state=on&duration=1")
//    sendEvent(name: "switch", value: "on")
}

def off() {

	off(1)  		// sets default Off TransitionTime    
    
}



def off(Number inTransitionTime) {
	log.debug "FLEXiLiFX(${device.label}): Executing 'Off(${inTransitionTime})'"
    if (device.currentValue("switch") == "on" ) {
		sendEvent(name: "switch", value: "off", isStateChange: true)
    }
    
	sendCommand("lights/"+device.deviceNetworkId+"/power", "PUT", "state=off&duration="+inTransitionTime)

}

def refresh() {
    sendCommand("lights/"+device.deviceNetworkId)
}

def poll() {
    refresh()
}

def setAdjustedColor(value) {

	log.debug "FLEXiLiFX: setAdjustedColor: ${value}"
	def adjusted = value + [:]
	adjusted.hue = adjustOutgoingHue(value.hue)
	adjusted.level = null // needed because color picker always sends 100
	setColor(adjusted)
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
	log.info "FLEXiLiFX: percent: $percent, adjusted: $adjusted"
	adjusted
}