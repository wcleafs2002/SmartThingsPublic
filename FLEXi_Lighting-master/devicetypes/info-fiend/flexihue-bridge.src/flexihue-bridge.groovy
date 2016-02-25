/**
 *  FLEXiHue Hue Bridge
 *
 *  Author: SmartThings / Anthony Pastor
 */
// for the UI

metadata {
	// Automatically generated. Make future change here.
	definition (name: "FLEXiHue Bridge", namespace: "info_fiend", author: "Anthony Pastor") {
		capability "Refresh"  
        
		attribute "serialNumber", "string"		
		attribute "networkAddress", "string"        
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		standardTile("icon", "icon", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "default", label: "FLEXiHue Bridge", action: "", icon: "st.Lighting.light99-hue", backgroundColor: "#3399FF"
		}
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
		valueTile("serialNumber", "device.serialNumber", decoration: "flat", height: 1, width: 2, inactiveLabel: false) {
			state "default", label:'SN: ${currentValue}'
		}
		valueTile("networkAddress", "device.networkAddress", decoration: "flat", height: 1, width: 2, inactiveLabel: false) {
			state "default", label:'${currentValue}', height: 1, width: 2, inactiveLabel: false
		}
		main (["icon"])
		details(["networkAddress", "refresh", "serialNumber"])
	}
}

// parse events into attributes
def parse(description) {
	def results = []
	def result = parent.parse(this, description)

	if (result instanceof physicalgraph.device.HubAction){
		results << result
	} else if (description == "updated") {
		//do nothing
		log.debug "FLEXiHue Bridge was updated"
	} else {
		def map = description
		if (description instanceof String)  {
			map = stringToMap(description)
		}
		if (map?.name && map?.value) {
			results << createEvent(name: "${map?.name}", value: "${map?.value}")
		}
		else {
			log.trace "FLEXiHue BRIDGE, OTHER"
			def msg = parseLanMessage(description)
			if (msg.body) {
				def contentType = msg.headers["Content-Type"]
				if (contentType?.contains("json")) {
					def bulbs = new groovy.json.JsonSlurper().parseText(msg.body)
					//log.info "BULBS: $bulbs"
					if (bulbs.state) {
						log.warn "NOT PROCESSED: $msg.body"
					}
                    
					else {
						log.debug "FLEXiHue BRIDGE, GENERATING BULB LIST EVENT"
                       
                        	log.trace "Sending Bulb List"
							sendEvent(name: "bulbList", value: device.hub.id, isStateChange: true, data: bulbs)

					}
				}
				else if (contentType?.contains("xml")) {
					log.debug "FLEXiHue HUE BRIDGE, SWALLOWING BRIDGE DESCRIPTION RESPONSE -- BRIDGE ALREADY PRESENT"
				}
			}
		}
	}
	results   
}

def poll() {
	log.debug "Executing 'polling'"
	parent.poll()
}

def refresh() {
	log.debug "Executing 'refresh'"
	parent.poll()
}

