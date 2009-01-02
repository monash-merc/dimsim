from django.http import HttpResponse
from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response
from urllib2 import URLError, urlopen
from settings import TEMPERATURE_URL
from settings import HUMIDITY_URL
import time

SENSOR_HISTORY_LENGTH = 120
SENSOR_UPDATE_TIME = 5
CURRENT_TEMP = 0.0
CURRENT_HUMID = 0.0
NEW_DATA = 'true'
ENCODE_URL = ''

def get_google_chart_text_encoding(d):
	"""
	"""
	vals = []
	currTime = time.time()
	for key in d:
		vals.append(','.join([("%06.2f" % ((currTime - i["time"])/60)) for i in d[key]]))
		vals.append(','.join([i["value"] for i in d[key]]))
	return 'chdlp=t&chd=t:%s&chdl=%s' % ('|'.join(vals), '|'.join(d.keys()).replace(' ', '%20'))

def table(request):
        global CURRENT_TEMP
        global CURRENT_HUMID
        """Returns a html snippet that draws a graph of the sensor data that
is listed in the database."""
        variables = {}
        
	if (CURRENT_TEMP != "Unavailable") :
		variables["temp"] = "%6.2f" % CURRENT_TEMP
        else :
		variables["temp"] = CURRENT_TEMP

        if (CURRENT_HUMID != "Unavailable") :
		variables["humid"] = "%6.2f" % CURRENT_HUMID
	else : 
		variables["humid"] = CURRENT_HUMID

        return render_to_response("sensor_table.html", variables)


def graphurl(request,source=0):
	"""Returns a html snippet that draws a graph of the sensor data that
is listed in the database."""
	return HttpResponse(graphEncode())

def graphEncode(source=0) :
	global ENCODE_URL
	global NEW_DATA

	data = {}
	if (source == 1) :
		data = past_hourly_data
		period = 'hours'
	else :
		data = past_data
		period = 'minutes'

	get_latest_data()
	if NEW_DATA == 'true' :
 
		ENCODE_URL = 'http://chart.apis.google.com/chart?cht=lxy&chs=%sx%s&%s&chco=%s&chxt=%s&chg=%s&chxl=2:|Time(%s) past&chxp=2,20&chxs=%s&chf=bg,s,DFE8F6|c,s,FFFFFF' % (
		400, 240,
		get_google_chart_text_encoding(data),
		'2345FE,DCBA09',
#		colors,
		'x,y,x',
#		past_data['Temperature'].getRange(0,1),
		'-1,10',
		period,
		'2,0000DD,13'
		)
		NEW_DATA = 'false'

	return ENCODE_URL

def data(request,source=0):
	a = lambda : graphEncode(source)

	_frontbuf = 0
	_backbuf = 1

	_buf = [
		request.session.get('buf0', a()),
		request.session.get('buf1', a())
		]

	_buf[_frontbuf] = _buf[_backbuf]
	_buf[_backbuf] = a()

	request.session['buf0'] = _buf[_frontbuf]
	request.session['buf1'] = _buf[_backbuf]

	return render_to_response('sensors/chart.html', {'buf0_url': _buf[_frontbuf], 'buf1_url': _buf[_backbuf]})

def old(request):
	"""Returns a html table that contains the current data outputted by the sensors."""
#	print reverse("sensors_data")
	get_latest_data()
	a = lambda: 'http://chart.apis.google.com/chart?cht=lxy&chs=%sx%s&%s&chco=%s&chxt=%s&chxr=%s&chg=%s&chf=bg,s,DFE8F6|c,s,FFFFFF' % (
		400, 240,
		get_google_chart_text_encoding(past_data),
		'2345FE,DCBA09',
#		colors,
		'x,y',
		'0,0,80',
#		past_data['Temperature'].getRange(0,1),
		'-1,10',
		)

	_frontbuf = 0
	_backbuf = 1

	_buf = [
		request.session.get('buf0', a()),
		request.session.get('buf1', a())
		]

	_buf[_frontbuf] = _buf[_backbuf]
	_buf[_backbuf] = a()

	request.session['buf0'] = _buf[_frontbuf]
	request.session['buf1'] = _buf[_backbuf]

	return render_to_response('sensors/chart.html', {'buf0_url': _buf[_frontbuf], 'buf1_url': _buf[_backbuf]})

#This dictionary stores sets of recent data:
#past_data = {
# eachSensorFromDB: [
#  {"time": datestamp, "value": value of sample}
# ]
#}


class SizedQueue(list):
	""" A leaky queue. 
	Has limited storage.
	The head gets dropped if append() is called
	when the queue is full.
	"""
	def __init__(self, list=[], max=SENSOR_HISTORY_LENGTH):
		self._max = max
#		self.extend(list)
	def append(self, y):
		if len(self) >= self._max:
			list.remove(self, self[0])
		list.append(self, y)
	def getRange(self,pos,scale):
		if scale < 1:
			scale = 1
		length = len(self)
		if length > 0 :
			first = self[0]["time"] / scale
			last = self[length - 1]["time"] / scale
		else :
			first = 0
			last = first
		retStr = '%s,0,%s' % (pos, ((last - first) / scale))  
		print 'returning ' + retStr
		return retStr

def get_latest_data():
	global TEMPERATURE_URL
	global HUMIDITY_URL
	"""Obtains up-to-date sampled data."""
#	sens = ({"name": 'Temperature','url': 'http://jainis.med.monash.edu.au:8001/temperature',},{"name": 'Humidity','url': 'http://jainis.med.monash.edu.au:8001/humidity',})
#	fill('Temperature','http://jainis.med.monash.edu.au/labjack/temperature')
#	fill('Humidity','http://jainis.med.monash.edu.au/labjack/humidity')
	fill('Temperature',TEMPERATURE_URL)
	fill('Humidity',HUMIDITY_URL)


past_data = {}
past_hourly_data = {}
def fill(name,url):
        global CURRENT_TEMP
        global CURRENT_HUMID
	global NEW_DATA

	t = name
	#Check to see that the sensor exists in our dictionary.
	if not past_data.has_key(t):
		past_data[t] = SizedQueue(SENSOR_HISTORY_LENGTH) 
		past_hourly_data[t] = SizedQueue(SENSOR_HISTORY_LENGTH) 
		print 'new array created for ' + t
	#See if there are any new enough values
	value = None
	for value_pair in past_data[t]:
		if (time.time() - value_pair["time"]) < SENSOR_UPDATE_TIME:
			vtime = value_pair["time"]
			value = value_pair["value"]
			break

	if not value:
		NEW_DATA='true'
		#Didn't break, so no available data. Have to fetch.
		try:
			print 'opening ' + url
			remote = urlopen(url)
			value = remote.read()
			remote.close()
                        if  (name == 'Temperature'):
                                CURRENT_TEMP = float(value)
                        else:
                                CURRENT_HUMID = float(value)
		except URLError, e:
			value = "Unavailable"
	
		past_data[t].append({"time":time.time(), "value":value})
	#We should have a value now of some sort.
		
