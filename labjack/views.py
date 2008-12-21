from labjack_interface import Labjack
from django.http import HttpResponse

labjack = Labjack()

def get_data(request, data_name):
    (tempC, tempF, humidity) = labjack.take_sample()
    if data_name[-1]=='/':
        data_name=data_name[0:-1]
    if data_name == "temperature":
        return HttpResponse(str(tempC))
    elif data_name == "humidity":
        return HttpResponse(str(humidity))
    else:
        return HttpResponse("%s is not a recognised sensor name." % data_name)
