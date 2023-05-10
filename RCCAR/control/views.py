from django.shortcuts import render
from django.http import JsonResponse
from . import driveControlSub

# Create your views here.
def drive(request):
    msg = {'result' : 'success'}
    
    return JsonResponse(msg)