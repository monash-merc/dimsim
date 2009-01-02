from django.conf.urls.defaults import *

urlpatterns = patterns('',
    #Note that the names are used in /templates/portal.html
    url(r'^data$', 'portal.portal.sensors.views.data', name='sensors_data'),
    url(r'^curr$', 'portal.portal.sensors.views.table', name='sensors_curr'),
    url(r'^graphurl$', 'portal.portal.sensors.views.graphurl', name='sensors_graph'),
)
