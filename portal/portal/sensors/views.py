from django.http import HttpResponse
from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response
from urllib2 import URLError, urlopen
import time

SENSOR_HISTORY_LENGTH = 30
SENSOR_UPDATE_TIME = 5
CURRENT_TEMP = 0
CURRENT_HUMID = 0

def get_google_chart_text_encoding(d):
	"""
	"""
	vals = []
	for key in d:
		vals.append(','.join([i['value'] for i in d[key]]))
#		vals.append(','.join([str(i) for i in d[key]]))
	return 'chdlp=t&chd=t:%s&chdl=%s' % ('|'.join(vals), '|'.join(d.keys()).replace(' ', '%20'))

def table(request):
        global CURRENT_TEMP
        global CURRENT_HUMID
        """Returns a html snippet that draws a graph of the sensor data that
is listed in the database."""
        variables = {}
        variables["temp"] = CURRENT_TEMP
        variables["humid"] = CURRENT_HUMID
        return render_to_response("sensor_table.html", variables)

def graph(request):
	"""Returns a html snippet that draws a graph of the sensor data that
is listed in the database."""
	return HttpResponse("Test")


def data(request):
	"""Returns a html table that contains the current data outputted by the sensors."""
#	print reverse("sensors_data")
	get_latest_data()
	a = lambda: 'http://chart.apis.google.com/chart?cht=lc&chs=%sx%s&%s&chco=%s&chxt=%s&chg=%s&chf=bg,s,DFE8F6|c,s,FFFFFF' % (
		400, 240,
		get_google_chart_text_encoding(past_data),
		'2345FE,DCBA09',
#		colors,
		'y',
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
	def __init__(self, list=[], max=35):
		self._max = max
#		self.extend(list)
	def append(self, y):
		if len(self) >= self._max:
			list.remove(self, self[0])
		list.append(self, y)


past_data = {}
def get_latest_data():
	"""Obtains up-to-date sampled data."""
#	sens = ({"name": 'Temperature','url': 'http://jainis.med.monash.edu.au:8001/temperature',},{"name": 'Humidity','url': 'http://jainis.med.monash.edu.au:8001/humidity',})
	fill('Temperature','http://jainis.med.monash.edu.au/labjack/temperature')
	fill('Humidity','http://jainis.med.monash.edu.au/labjack/humidity')


def fill(name,url):
        global CURRENT_TEMP
        global CURRENT_HUMID
	t = name
	#Check to see that the sensor exists in our dictionary.
	if not past_data.has_key(t):
		past_data[t] = SizedQueue(SENSOR_HISTORY_LENGTH) 
		print 'new array created for ' + t
	#See if there are any new enough values
	value = None
	for value_pair in past_data[t]:
		if (time.time() - value_pair["time"]) < SENSOR_UPDATE_TIME:
			value = value_pair["value"]
			break

	if not value:
		#Didn't break, so no available data. Have to fetch.
		try:
			print 'opening ' + url
			remote = urlopen(url)
			value = remote.read()
			remote.close()
                        if  (name == 'Temperature'):
                                CURRENT_TEMP = value
                        else:
                                CURRENT_HUMID = value
		except URLError, e:
			value = "Unavailable"
	
	past_data[t].append({"time":time.time(), "value":value})
	#We should have a value now of some sort.
		
