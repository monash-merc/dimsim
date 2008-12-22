from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response
from django.template.context import RequestContext
from django.http import HttpResponse
from django.http import HttpResponseRedirect

import time

from settings import MEDIA_URL
from settings import PROJINFO_URL
from settings import CRYSTALCAM_URL


#Serves the main portal page. This page is templated so
#that it will be automatically configured to display
#the ImagePortlets configured in the database using
#the Django admin tool.

def portal(request):

    variables = {}
    variables["MEDIA_URL"] = MEDIA_URL    
    variables["webcams"] = [{'name':'CrystalCam', 
		'url': CRYSTALCAM_URL,
		'refresh_time':'10',}]

    return render_to_response('portal.html', variables)


def latest_image(request):
        variables = {}
        variables["MEDIA_URL"] = MEDIA_URL
	return render_to_response('latest_image.html',variables)

def projinfo(request):
	if not request.META['HTTP_HOST'].endswith(PROJINFO_URL):
		return HttpResponseRedirect(PROJINFO_URL)

