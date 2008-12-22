from django.conf.urls.defaults import *
from settings import MEDIA_ROOT

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
 #Any 'sensors' subdirectory requests should be handled by the
    #sensors app.
    url(r'^latest.*', 'portal.portal.views.latest_image', name='latest_scan'),
    url(r'^sensors/', include('portal.portal.sensors.urls')),
    #The site root should show the portal page.
    url(r'^$', 'portal.portal.views.portal', name='portal'),
    url(r'^projinfo$', 'portal.portal.views.projinfo', name='projinfo'),
    url(r'^image_block$', 'portal.portal.views.latest_image', name='latest_image'),
    url(r'^media/(?P<path>.*)$', 'django.views.static.serve', {'document_root': MEDIA_ROOT, 'show_indexes':True}),
    (r'^admin/(.*)', admin.site.root),
)
