from django.conf.urls.defaults import *

urlpatterns = patterns('',
    # Any connection is assumed to be requesting data.
    (r'^(?P<data_name>.+)$', 'labjack.views.get_data'),
)
