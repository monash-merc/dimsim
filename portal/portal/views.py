from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response
from django.template.context import RequestContext
from django.http import HttpResponse

from elementtree import ElementTree as ET
import time

from settings import MEDIA_URL
from settings import DIMSIM_HOST_URL
from settings import PORTAL_HOST_URL
from settings import CRYSTALCAM_URL
from settings import SOURCE_ID
from settings import SOURCEBuffer_ID
#from portal.models import ImagePortlet
from portal.soap.SOAPTransportService_services import *

from os.path import split
#CRYSTALCAM_URL= "http://jainis.med.monash.edu.au:8003"
#DIMSIM_HOST_URL= "http://localhost:8080/Cima_Webapp"
#PORTAL_HOST_URL= "http://localhost/portal"
#SOURCE_ID = "Rigaku_Monash"
#SOURCEBuffer_ID = "Buffer_Plugin"
SESSION_ID = ""
BufferSESSION_ID = ""
PORTAL_ID = "Cima_Portal"
UNKNOWN = "Unknown"
PROJECT_NAME = UNKNOWN
SAMPLE_NAME = UNKNOWN
COUNT = 0

# the parcel sequence id - not really used in CIMA yet
OUTGOING_SEQUENCE_ID = 0

SOAP_RESPONSE_ENVELOPE = """<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><handleParcelRes xmlns="http://cima.instrumentmiddleware.org/ws">%(PARCEL)s</handleParcelRes></soap:Body></soap:Envelope>"""

SOAP_NS = 'http://schemas.xmlsoap.org/soap/envelope/'
CIMA_SOAP_NS = 'http://cima.instrumentmiddleware.org/ws'
CIMA_XTAL_NS = 'http://archer.edu.au/cima/xtal'
CIMA_NS = 'http://cima.instrumentmiddleware.org/parcel'
XSI_NS = 'http://www.w3.org/2001/XMLSchema-instance'
NS_DICT = {
	'CIMA_NS' : CIMA_NS,
	'SOAP_NS' : SOAP_NS,
	'CIMA_SOAP_NS' : CIMA_SOAP_NS,
	'CIMA_XTAL_NS' : CIMA_XTAL_NS,
	'XSI_NS' : XSI_NS,
}



#Serves the main portal page. This page is templated so
#that it will be automatically configured to display
#the ImagePortlets configured in the database using
#the Django admin tool.

def portal(request):
    global MEDIA_URL 
    global CRYSTALCAM_URL
#Subscribe to Dimsim/Cima
    subscribe(request)

    variables = {}
    variables["MEDIA_URL"] = MEDIA_URL    
    variables["webcams"] = [{'name':'CrystalCam', 
		'url':CRYSTALCAM_URL, 
		'refresh_time':'10',}]

    return render_to_response('portal.html', variables)


def latest_image(request):
        variables = {}
        variables["MEDIA_URL"] = MEDIA_URL
	return render_to_response('latest_image.html',variables)


def soap(request):
	global PROJECT_NAME
	global SAMPLE_NAME
	global COUNT

#	print request.raw_post_data
	doc = ET.fromstring(request.raw_post_data)
#	print doc
	for val in doc.findall('{%(SOAP_NS)s}Body/{%(CIMA_SOAP_NS)s}handleParcel' % NS_DICT):
		parcel = ET.fromstring(val.text)
		print val.text

	incomingSessionID = parcel.find('{%(CIMA_NS)s}sessions/{%(CIMA_NS)s}session/{%(CIMA_NS)s}sessionId' % NS_DICT).text

#	print parcel
	sequenceId = parcel.get('sequenceId')

	try:
		print "getting type"
		type = parcel.find('{%(CIMA_NS)s}type' % NS_DICT).text
		print "getting body"
		body = parcel.find('{%(CIMA_NS)s}body' % NS_DICT)
		print "done"
	except:
		print "error getting type or body"


	print "type: %s" % type


	# we should never get this here!
	if type == 'response':
		pass
	else:
		if body is not None:
			bodyType = body.get('{%(XSI_NS)s}type' % NS_DICT)
			print "bodyType: %s" % bodyType

			if bodyType == 'xtal:beginXtalExperimentType':
				COUNT = 0
				PROJECT_NAME = body.find('{%(CIMA_NS)s}ProjectName' % NS_DICT).text
				SAMPLE_NAME = body.find('{%(CIMA_XTAL_NS)s}SampleName' % NS_DICT).text
			elif bodyType == 'xtal:endXtalExperimentType':
				PROJECT_NAME = UNKNOWN
				SAMPLE_NAME = UNKNOWN
				COUNT = 0
			elif bodyType == 'xtal:xtalImageType':
				COUNT = COUNT + 1
				checkProjInfo()

	parcel = """<parcel version="0.1" sequenceId="%(SEQUENCE_ID)s" xmlns="http://cima.instrumentmiddleware.org/parcel" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
		<type>response</type>
		<creationTime>%(TIME)s.000+10:00</creationTime>
		<sessions>
			<session>
				<sessionId>%(SESSION_ID)s</sessionId>
			</session>
		</sessions>
		<body xsi:type="par:responseBodyType" xmlns:par="http://cima.instrumentmiddleware.org/parcel">
			<response>
				<sessionId>%(SESSION_ID)s</sessionId>
			</response>
		</body>
	</parcel>""" % { 'SEQUENCE_ID' : sequenceId, 'TIME' : time.strftime("%Y-%m-%dT%H:%M:%S", time.localtime()), 'SESSION_ID' : incomingSessionID }

	parcel = parcel.replace('<', '&lt;').replace('>', '&gt;').replace('"', '&quot;')
	print "soap response " + parcel

	response = HttpResponse(SOAP_RESPONSE_ENVELOPE % { "PARCEL": parcel }, mimetype="text/xml")
	response['SOAPAction'] = '""'
	response['Content-Type'] = 'text/xml; charset=utf-8'
	response['Content-Length'] = len(parcel)
	return response


# attempt to register with CIMA
def subscribe(request):
    global SESSION_ID
    global BufferSESSION_ID
    global PORTAL_ID
    global SOURCE_ID
    retStr=' '
    if SESSION_ID == '':
        SESSION_ID = generic_subscribe(SOURCE_ID, PORTAL_ID)
        
    if BufferSESSION_ID == '':
        BufferSESSION_ID = generic_subscribe(SOURCEBuffer_ID, PORTAL_ID)
    print "BufferSessionID = " + BufferSESSION_ID
	
    return HttpResponse('%(RET)s' % {'RET' : SESSION_ID + retStr + BufferSESSION_ID})

# generic subscirbe for reuse 
def generic_subscribe(sourceID, consumerID):
    global OUTGOING_SEQUENCE_ID
    global DIMSIM_HOST_URL
    global PORTAL_HOST_URL
	
    port = SOAPTransportServiceSoapBindingSOAP(DIMSIM_HOST_URL + '/ws/cima')
    parcel = handleParcel('''<parcel xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    	xmlns="http://cima.instrumentmiddleware.org/parcel" version="0.0" sequenceId="%(SEQUENCE)s">
				<type>subscribe</type>
				<creationTime>%(TIME)s.000+10:00</creationTime>
				<sender><id>%(CONSUMER)s</id></sender>
				<recipient><id>%(SOURCE)s</id></recipient>
				<body xsi:type="subscriptionRequestType">
					<endpoint type="SOAP">
						<url>%(ENDPOINT)s</url>
					</endpoint>
					<dataInterval>
						<value>10</value><unit>second</unit>
					</dataInterval>
				</body>
			</parcel>''' % { 'TIME' : time.strftime("%Y-%m-%dT%H:%M:%S", time.localtime()),
						    'SEQUENCE' : OUTGOING_SEQUENCE_ID,
						    'ENDPOINT' : PORTAL_HOST_URL + '/soap' , 
						    'SOURCE' : sourceID, 
						    'CONSUMER' : consumerID } )
    OUTGOING_SEQUENCE_ID += 1
    
    response = port.handleParcel(parcel)
    doc = ET.fromstring(response)
    
    retStr = " Subscribe request for " + sourceID + " is "
    retStatus = 'unavailable'
    for val in doc.findall('{%(CIMA_NS)s}body/{%(CIMA_NS)s}response' % NS_DICT):
    	retStatus =  val.find('{%(CIMA_NS)s}status' % NS_DICT).text
    	if retStatus == 'success' :
    		sessionID = val.find('{%(CIMA_NS)s}newSessionId' % NS_DICT).text
    		print "Got session id %(1)s for source %(2)s" % {'1':sessionID,'2': sourceID}
    	retStr = retStr + retStatus

    return sessionID

			
def unsubscribe(sessionID):
	global OUTGOING_SEQUENCE_ID
	global DIMSIM_HOST_URL
	global PORTAL_HOST_URL
	
	port = SOAPTransportServiceSoapBindingSOAP(DIMSIM_HOST_URL + '/ws/cima')
	parcel = handleParcel('''<parcel xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    	xmlns="http://cima.instrumentmiddleware.org/parcel" version="0.0" sequenceId="%(SEQUENCE)s">
				<type>unsubscribe</type>
				<creationTime>%(TIME)s.000+10:00</creationTime>
				<sessions><session><sessionId>%(SESSION_ID)s</sessionId></session></sessions>
				<body xsi:type="subscriptionRequestType">
					<endpoint type="SOAP">
						<url>%(ENDPOINT)s</url>
					</endpoint>
					<dataInterval>
						<value>10</value><unit>second</unit>
					</dataInterval>
				</body>
			</parcel>''' % { 'TIME' : time.strftime("%Y-%m-%dT%H:%M:%S", time.localtime()),
						    'SEQUENCE' : OUTGOING_SEQUENCE_ID,
						    'ENDPOINT' :  PORTAL_HOST_URL + '/soap',
						    'SESSION_ID' : sessionID 
							} )
	
	OUTGOING_SEQUENCE_ID += 1
	
	response = port.handleParcel(parcel)

	return render_to_response('portal.html', variables)


def get_proj_info(request):
	global PROJECT_NAME
	global SAMPLE_NAME
	global COUNT
#	global MEDIA_URL

	variables = {}
	variables["project"] = PROJECT_NAME
	variables["sample"] = SAMPLE_NAME
	variables["imagecount"] = COUNT
        variables["MEDIA_URL"] = MEDIA_URL    
	return render_to_response('proj_info.html', variables)


def checkProjInfo():
	global PROJECT_NAME
	global UNKNOWN

	if PROJECT_NAME == UNKNOWN :
		getBufferProjInfo();

def testBufferProjInfo(request):
	getBufferProjInfo();
	return get_proj_info(request)

def getBufferProjInfo():
	global OUTGOING_SEQUENCE_ID
	global DIMSIM_HOST_URL
	global PORTAL_HOST_URL
    	global BufferSESSION_ID
    	global SOURCE_ID
	
	port = SOAPTransportServiceSoapBindingSOAP(DIMSIM_HOST_URL + '/ws/cima')
    	parcelP = '''<parcel xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    	xmlns="http://cima.instrumentmiddleware.org/parcel" version="0.0" sequenceId="%(SEQUENCE)s">
	<type>plugin</type>
	<creationTime>%(TIME)s.000+10:00</creationTime>
	<sessions><session>
		<sessionId>%(SESSION_ID)s</sessionId>
	</session></sessions>
	<body xsi:type="commandOperationType">
    	<commandOperation>
      		<commandName>getLastParcel</commandName>
      		<parameter>
        		<name>bodyType</name>
        		<value>au.edu.archer.cima.xtal.impl.BeginXtalExperimentTypeImpl</value>
      		</parameter>
      		<parameter>
        		<name>pluginId</name>
        		<value>%(SOURCE_ID)s</value>
      		</parameter>
    	</commandOperation>
	</body>
	</parcel>''' % { 'TIME' : time.strftime("%Y-%m-%dT%H:%M:%S", time.localtime()),
						    'SEQUENCE' : OUTGOING_SEQUENCE_ID,
						    'SESSION_ID' : BufferSESSION_ID, 
						    'SOURCE_ID' : SOURCE_ID 
							}
	
    	parcel = handleParcel(parcelP);
	OUTGOING_SEQUENCE_ID += 1
	
	response = port.handleParcel(parcel)

