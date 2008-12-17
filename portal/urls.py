from django.conf.urls.defaults import *
from settings import MEDIA_ROOT

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
 #Any 'sensors' subdirectory requests should be handled by the
    #sensors app.
    url(r'^subscribe.*', 'portal.views.subscribe'),
    url(r'^test.*', 'portal.views.testBufferProjInfo'),
    url(r'^latest.*', 'portal.views.latest_image', name='latest_scan'),
    url(r'^projinfo.*', 'portal.views.get_proj_info', name='proj_info'),
    url(r'^soap', 'portal.views.soap', name='soap'),
    url(r'^sensors/', include('portal.sensors.urls')),
    #The site root should show the portal page.
    url(r'^$', 'portal.views.portal', name='portal'),
    url(r'^image_block$', 'portal.views.latest_image', name='latest_image'),
    url(r'^media/(?P<path>.*)$', 'django.views.static.serve', {'document_root': MEDIA_ROOT, 'show_indexes':True}),
    (r'^admin/(.*)', admin.site.root),
)
