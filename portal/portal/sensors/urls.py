from django.conf.urls.defaults import *

urlpatterns = patterns('',
    #Note that the names are used in /templates/portal.html
    url(r'^data$', 'portal.sensors.views.data', name='sensors_data'),
    url(r'^curr$', 'portal.sensors.views.table', name='sensors_curr'),
    url(r'^graph$', 'portal.sensors.views.graph', name='sensors_graph'),
)
